import torch
import torch.nn as nn


class NCF(nn.Module):
    def __init__(self, num_users, num_items, embedding_dim=32, layers=[64, 32, 16]):
        super(NCF, self).__init__()

        # --- GMF 部分 (广义矩阵分解) ---
        self.gmf_user_embedding = nn.Embedding(num_users, embedding_dim)
        self.gmf_item_embedding = nn.Embedding(num_items, embedding_dim)

        # --- MLP 部分 (多层感知机) ---
        self.mlp_user_embedding = nn.Embedding(num_users, embedding_dim)
        self.mlp_item_embedding = nn.Embedding(num_items, embedding_dim)

        self.mlp_layers = nn.Sequential()
        input_size = embedding_dim * 2
        for i, layer_size in enumerate(layers):
            self.mlp_layers.add_module(f'linear_{i}', nn.Linear(input_size, layer_size))
            self.mlp_layers.add_module(f'relu_{i}', nn.ReLU())
            input_size = layer_size

        # --- NeuMF 层 (合并层) ---
        # 输入维度 = GMF输出(embedding_dim) + MLP输出(layers[-1])
        predict_input_size = embedding_dim + layers[-1]
        self.predict_layer = nn.Linear(predict_input_size, 1)

        # 输出层 (预测评分/概率)
        # 如果是预测评分(1-5)，最后不用 Sigmoid；如果是预测点击概率(0-1)，用 Sigmoid
        # 这里我们为了简单，直接预测一个分数

    def forward(self, user_indices, item_indices):
        # GMF Forward
        gmf_u = self.gmf_user_embedding(user_indices)
        gmf_i = self.gmf_item_embedding(item_indices)
        gmf_vector = torch.mul(gmf_u, gmf_i)  # 逐元素相乘

        # MLP Forward
        mlp_u = self.mlp_user_embedding(user_indices)
        mlp_i = self.mlp_item_embedding(item_indices)
        mlp_vector = torch.cat([mlp_u, mlp_i], dim=-1)  # 拼接
        mlp_vector = self.mlp_layers(mlp_vector)

        # Concatenate
        vector = torch.cat([gmf_vector, mlp_vector], dim=-1)

        # Final Prediction
        output = self.predict_layer(vector)
        return output.view(-1)  # 展平为一维