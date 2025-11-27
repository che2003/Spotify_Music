import os
from mutagen.flac import FLAC
import syncedlyrics
import time

# ================= 配置区域 =================
INPUT_FOLDER = "./music/flac"  # FLAC 所在文件夹
OUTPUT_FOLDER = "./music_information/lrc"  # 结果输出总目录


# ===========================================

def sanitize_filename(name):
    """清理文件名，确保和之前的 MP3 命名一致"""
    return name.replace("/", "&").replace("\\", "&").replace(":", " ").replace("?", "").replace('"', '').replace('*',
                                                                                                                 '').replace(
        '<', '').replace('>', '').replace('|', '')


def download_lyrics(input_dir, output_root):
    # 1. 准备歌词输出路径
    lrc_dir = os.path.join(output_root, "所有歌词(Lyrics)")
    if not os.path.exists(lrc_dir):
        os.makedirs(lrc_dir)

    # 2. 获取文件列表
    if not os.path.exists(input_dir):
        print(f"❌ 找不到输入文件夹: {input_dir}")
        return

    files = [f for f in os.listdir(input_dir) if f.lower().endswith('.flac')]
    total = len(files)
    print(f"🚀 准备为 {total} 首歌搜索歌词...\n")

    success_count = 0

    for index, filename in enumerate(files):
        file_path = os.path.join(input_dir, filename)

        try:
            # === 读取标签用于搜索 ===
            audio = FLAC(file_path)
            title = audio.get("TITLE", [""])[0]
            artist = audio.get("ARTIST", [""])[0]

            # 如果标签是空的，就尝试用文件名解析（作为备用）
            if not title or not artist:
                base_name = os.path.splitext(filename)[0]
                # 假设文件名是 "歌手 - 歌名" 格式
                if " - " in base_name:
                    parts = base_name.split(" - ")
                    artist = parts[0]
                    title = parts[1]
                else:
                    title = base_name  # 实在没办法，只搜歌名

            # === 构造搜索关键词 ===
            # 去掉一些干扰词，比如 (Explicit), (Live) 等，这样搜索命中率更高
            clean_title = title.split("(")[0].strip()
            search_term = f"{artist} {clean_title}"

            # === 构造输出文件名 ===
            # 必须和之前的 MP3 命名逻辑完全一致： "歌手 - 歌名.lrc"
            file_naming = sanitize_filename(f"{artist} - {title}")
            lrc_path = os.path.join(lrc_dir, f"{file_naming}.lrc")

            print(f"[{index + 1}/{total}] 🔍 正在搜索: {search_term}")

            # === 如果已经存在就不重复下载 ===
            if os.path.exists(lrc_path):
                print("   ⏩ 跳过 (已存在)")
                continue

            # === 调用库下载歌词 ===
            # providers=["netease", "qq"] 表示优先搜网易云和QQ音乐
            lrc_content = syncedlyrics.search(search_term, providers=["netease", "qq", "musixmatch"])

            if lrc_content:
                with open(lrc_path, "w", encoding="utf-8") as f:
                    f.write(lrc_content)
                print(f"   ✅ 下载成功")
                success_count += 1
            else:
                print(f"   ⚠️ 未找到歌词")

            # 稍微停顿一下，防止请求太快被封IP
            time.sleep(1)

        except Exception as e:
            print(f"   ❌ 出错: {e}")

    print(f"\n🎉 处理完成！成功获取 {success_count}/{total} 首歌曲的歌词。")
    print(f"📂 歌词保存在: {lrc_dir}")


# 运行
download_lyrics(INPUT_FOLDER, OUTPUT_FOLDER)