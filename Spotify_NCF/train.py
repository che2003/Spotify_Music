import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader, TensorDataset
import pandas as pd
import json
import os
import matplotlib.pyplot as plt
from data_reader import get_interaction_data
from ncf_model import NCF

# Config
MODEL_PATH = "ncf_model.pth"
MAPPING_PATH = "mappings.json"
PLOT_PATH = "training_result.png"
EMBEDDING_DIM = 32
EPOCHS = 50
LR = 0.001

def train():
    print("[INFO] 1. Loading data from database...")
    df, user_to_index, song_to_index, _, _ = get_interaction_data()

    if df is None or len(df) == 0:
        print("[ERROR] No data found. Cannot train.")
        return

    # Save mappings
    mappings = {
        "user_to_index": user_to_index,
        "song_to_index": song_to_index,
        # Reverse mapping: index -> song_id
        "index_to_song": {int(v): int(k) for k, v in song_to_index.items()}
    }
    with open(MAPPING_PATH, 'w') as f:
        json.dump(mappings, f)
    print(f"[INFO] Mappings saved to {MAPPING_PATH}")

    # Prepare Dataset
    users = torch.LongTensor([user_to_index[u] for u in df['user_id'].values])
    songs = torch.LongTensor([song_to_index[s] for s in df['song_id'].values])
    ratings = torch.FloatTensor(df['rating'].values)

    dataset = TensorDataset(users, songs, ratings)
    dataloader = DataLoader(dataset, batch_size=16, shuffle=True)

    # Init Model
    num_users = len(user_to_index)
    num_items = len(song_to_index)
    model = NCF(num_users, num_items, embedding_dim=EMBEDDING_DIM)

    criterion = nn.MSELoss()
    optimizer = optim.Adam(model.parameters(), lr=LR)

    print(f"[INFO] 2. Starting training (Users: {num_users}, Songs: {num_items})...")

    loss_history = []

    model.train()
    for epoch in range(EPOCHS):
        total_loss = 0
        for u_batch, i_batch, r_batch in dataloader:
            optimizer.zero_grad()
            predictions = model(u_batch, i_batch)
            loss = criterion(predictions, r_batch)
            loss.backward()
            optimizer.step()
            total_loss += loss.item()

        avg_loss = total_loss / len(dataloader)
        loss_history.append(avg_loss)

        print(f"Epoch {epoch+1}/{EPOCHS} - Loss: {avg_loss:.4f}")

    # Save Model
    torch.save(model.state_dict(), MODEL_PATH)
    print(f"[SUCCESS] Model saved to {MODEL_PATH}")

    # Plotting
    print("[INFO] 3. Generating training loss plot...")
    plt.figure(figsize=(10, 6))
    plt.plot(range(1, EPOCHS + 1), loss_history, marker='o', linestyle='-', color='b', label='Training Loss')
    plt.title('NCF Model Training Loss Over Epochs')
    plt.xlabel('Epoch')
    plt.ylabel('MSE Loss')
    plt.grid(True)
    plt.legend()

    plt.savefig(PLOT_PATH)
    print(f"[INFO] Plot saved to: {PLOT_PATH}")

if __name__ == "__main__":
    train()