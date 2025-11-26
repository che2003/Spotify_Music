import pandas as pd
from sqlalchemy import create_engine

# Database Config
DB_USER = "root"
DB_PASSWORD = "123456"
DB_HOST = "localhost"
DB_NAME = "spotify_music"

def get_interaction_data():
    """
    Connect to MySQL and read interaction data.
    """
    DB_URL = f"mysql+pymysql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}/{DB_NAME}"

    try:
        engine = create_engine(DB_URL)
        print("[INFO] Successfully connected to MySQL.")

        # 1. Read interactions (Rating >= 1.0)
        query_interaction = "SELECT user_id, song_id, rating FROM user_interaction WHERE rating >= 1.0;"
        df_interaction = pd.read_sql(query_interaction, engine)

        if df_interaction.empty:
            print("[WARNING] No interaction data found in database! Please run SQL seed script first.")
            return None, None, None, None, None

        # 2. Create ID Mappings
        user_ids = df_interaction['user_id'].unique()
        song_ids = df_interaction['song_id'].unique()

        # Map Real_ID -> Index (0 ~ N-1)
        # Force int() conversion to avoid JSON serialization errors with numpy.int64
        user_to_index = {int(user_id): index for index, user_id in enumerate(user_ids)}
        song_to_index = {int(song_id): index for index, song_id in enumerate(song_ids)}

        print(f"[INFO] Data loaded. Interactions: {len(df_interaction)}")
        print(f"[INFO] Total Users: {len(user_ids)}, Total Songs: {len(song_ids)}")

        return df_interaction, user_to_index, song_to_index, user_ids, song_ids

    except Exception as e:
        print(f"[ERROR] Database connection or query failed!")
        print(f"[ERROR] Details: {e}")
        return None, None, None, None, None

if __name__ == '__main__':
    df, u_map, s_map, u_ids, s_ids = get_interaction_data()
    if df is not None:
        print("\n[DEBUG] Interaction Data Head:")
        print(df.head())
        print("\n[DEBUG] Mapping Example:")
        print(f"User ID Mapping: {list(u_map.items())[:3]}")