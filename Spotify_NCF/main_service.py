from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import torch
import json
import os
import random
from ncf_model import NCF
from data_reader import get_interaction_data

app = FastAPI(title="NCF Recommendation Service")

# Paths
MODEL_PATH = "ncf_model.pth"
MAPPING_PATH = "mappings.json"

# Globals
model = None
user_to_index = {}
index_to_song = {}
all_song_indices = []

def load_model_and_data():
    """Load trained model and mapping files."""
    global model, user_to_index, index_to_song, all_song_indices

    if not os.path.exists(MODEL_PATH) or not os.path.exists(MAPPING_PATH):
        print("[WARNING] Model or mapping file not found. Please run train.py first.")
        return False

    try:
        # 1. Load Mappings
        with open(MAPPING_PATH, 'r') as f:
            mappings = json.load(f)
            user_to_index = mappings['user_to_index']
            # JSON keys are strings, convert back to int
            index_to_song = {int(k): int(v) for k, v in mappings['index_to_song'].items()}

        # 2. Prepare indices
        all_song_indices = torch.LongTensor(list(index_to_song.keys()))

        # 3. Load Model
        num_users = len(user_to_index)
        num_items = len(index_to_song)
        model = NCF(num_users, num_items)

        # map_location='cpu' ensures it runs even without GPU
        model.load_state_dict(torch.load(MODEL_PATH, map_location=torch.device('cpu')))
        model.eval()

        print("[SUCCESS] Model loaded successfully!")
        return True
    except Exception as e:
        print(f"[ERROR] Failed to load model: {e}")
        return False

# Load on startup
model_loaded = load_model_and_data()

# Load fallback data
DF_INTERACTION, _, _, _, _ = get_interaction_data()
if DF_INTERACTION is not None:
    MOCK_SONG_IDS = DF_INTERACTION['song_id'].unique().tolist()
else:
    MOCK_SONG_IDS = []

class PredictionRequest(BaseModel):
    user_id: int
    k: int = 10

@app.post("/predict")
async def predict(req: PredictionRequest):
    # Check if model is ready and user exists in training data
    if not model_loaded or str(req.user_id) not in user_to_index:
        print(f"[INFO] User {req.user_id} using fallback strategy (Cold Start/Model Not Loaded)")
        if MOCK_SONG_IDS:
            return {
                "user_id": req.user_id,
                "recommendations": random.sample(MOCK_SONG_IDS, min(req.k, len(MOCK_SONG_IDS)))
            }
        else:
            return {"user_id": req.user_id, "recommendations": []}

    try:
        # Prepare input
        u_idx = user_to_index[str(req.user_id)]
        user_indices = torch.LongTensor([u_idx] * len(all_song_indices))

        # Predict
        with torch.no_grad():
            predictions = model(user_indices, all_song_indices)

        # Top K
        _, top_indices = torch.topk(predictions, min(req.k, len(all_song_indices)))

        # Map back to Song IDs
        recommend_ids = [index_to_song[idx.item()] for idx in top_indices]

        return {
            "user_id": req.user_id,
            "recommendations": recommend_ids
        }

    except Exception as e:
        print(f"[ERROR] Prediction error: {e}")
        return {"user_id": req.user_id, "recommendations": []}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="127.0.0.1", port=5000)