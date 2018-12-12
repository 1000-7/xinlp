import gensim
import numpy as np

import config

model = gensim.models.Word2Vec.load(config.embedding_char)

d = {1: model["人"], 2: model["武"]}
e = list(d.values())
x = np.array(e)
print(x.shape)
print(x)

embedding_mat = np.random.uniform(-0.25, 0.25, (2, 300))
embedding_mat = np.float32(embedding_mat)
print(embedding_mat.shape)
print(embedding_mat)
