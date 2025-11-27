import os
import pandas as pd
from pydub import AudioSegment
from mutagen.flac import FLAC
from mutagen.id3 import ID3, APIC, TIT2, TPE1, TALB, TYER

# ================= 配置区域 =================
INPUT_FOLDER = "./music/flac"
OUTPUT_FOLDER = "music_information"


# ===========================================

def sanitize_filename(name):
    """清理文件名中不合法的字符"""
    return name.replace("/", "&").replace("\\", "&").replace(":", " ").replace("?", "").replace('"', '').replace('*',
                                                                                                                 '').replace(
        '<', '').replace('>', '').replace('|', '')


def process_music_flat(input_dir, output_root):
    if not os.path.exists(input_dir):
        print(f"❌ 错误：找不到输入文件夹: {input_dir}")
        return

    all_data = []

    mp3_dir = os.path.join(output_root, "所有歌曲(MP3)")
    cover_dir = os.path.join(output_root, "所有封面(Covers)")

    for d in [output_root, mp3_dir, cover_dir]:
        if not os.path.exists(d):
            os.makedirs(d)

    files = [f for f in os.listdir(input_dir) if f.lower().endswith('.flac')]
    total = len(files)
    print(f"🚀 开始处理 {total} 个文件 (已启用自动降采样兼容 MP3)...\n")

    for index, filename in enumerate(files):
        file_path = os.path.join(input_dir, filename)
        print(f"[{index + 1}/{total}] 处理中: {filename}")

        try:
            # === 1. 读取信息 ===
            audio = FLAC(file_path)
            raw_title = audio.get("TITLE", [os.path.splitext(filename)[0]])[0]
            raw_artist = audio.get("ARTIST", ["Unknown Artist"])[0]
            album = audio.get("ALBUM", ["Unknown Album"])[0]
            date = audio.get("DATE", [""])[0]

            clean_name = sanitize_filename(f"{raw_artist} - {raw_title}")
            target_mp3_path = os.path.join(mp3_dir, f"{clean_name}.mp3")

            # === 2. 处理封面 ===
            cover_data = None
            cover_status = "无封面"
            ext = "jpg"

            if audio.pictures:
                pic = audio.pictures[0]
                cover_data = pic.data
                ext = "jpg" if "jpeg" in pic.mime else "png"
                target_cover_path = os.path.join(cover_dir, f"{clean_name}.{ext}")
                with open(target_cover_path, "wb") as f:
                    f.write(cover_data)
                cover_status = "已提取"

            # === 3. 转换 MP3 (修复版) ===
            if not os.path.exists(target_mp3_path):
                # 读取音频
                sound = AudioSegment.from_file(file_path, format="flac")

                # [关键修复]：检查采样率，如果超过 48000Hz，强制降采样
                if sound.frame_rate > 48000:
                    print(f"   📉 检测到高采样率 ({sound.frame_rate}Hz)，正在降频至 48000Hz 以兼容 MP3...")
                    sound = sound.set_frame_rate(48000)

                # 导出 MP3
                sound.export(target_mp3_path, format="mp3", bitrate="320k")

                # 写入标签
                mp3_audio = ID3(target_mp3_path)
                mp3_audio.add(TIT2(encoding=3, text=raw_title))
                mp3_audio.add(TPE1(encoding=3, text=raw_artist))
                mp3_audio.add(TALB(encoding=3, text=album))
                if date:
                    mp3_audio.add(TYER(encoding=3, text=date))

                if cover_data:
                    mp3_audio.add(APIC(
                        encoding=3,
                        mime=f'image/{("jpeg" if ext == "jpg" else "png")}',
                        type=3,
                        desc=u'Cover',
                        data=cover_data
                    ))
                mp3_audio.save()
            else:
                print("   ⏩ 跳过 (已存在)")

            all_data.append({
                "文件名": clean_name,
                "歌手": raw_artist,
                "歌名": raw_title,
                "专辑": album,
                "封面": cover_status,
                "MP3位置": target_mp3_path
            })

        except Exception as e:
            print(f"❌ 错误: {e}")
            all_data.append({"文件名": filename, "状态": f"错误: {str(e)}"})

    if all_data:
        df = pd.DataFrame(all_data)
        df.to_excel(os.path.join(output_root, "歌曲信息汇总.xlsx"), index=False)
        print("\n✅ 全部完成！")


# 运行
process_music_flat(INPUT_FOLDER, OUTPUT_FOLDER)