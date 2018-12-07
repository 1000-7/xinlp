# -*- coding: utf-8 -*-

from hanziconv import HanziConv
from jieba import cut
from tflearn.data_utils import VocabularyProcessor

DOCUMENTS = [
    '这是一条测试1',
    '这是一条测试2',
    '这是一条测试3',
    '这是其他测试',
]


def chinese_tokenizer(documents):
    """
    把中文文本转为词序列
    """

    for document in documents:
        # 繁体转简体
        text = HanziConv.toSimplified(document)
        # 英文转小写
        text = text.lower()
        # 分词
        yield list(cut(text))


# 序列长度填充或截取到100，删除词频<=2的词
vocab = VocabularyProcessor(100, 2, tokenizer_fn=chinese_tokenizer)

# 创建词汇表，创建后不能更改
vocab.fit(DOCUMENTS)

# 保存和加载词汇表
vocab.save('vocab.pickle')
vocab = VocabularyProcessor.restore('vocab.pickle')

# 文本转为词ID序列，未知或填充用的词ID为0
id_documents = list(vocab.transform(DOCUMENTS))
for id_document in id_documents:
    print(id_document)
# [2 3 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
#  0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
#  0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
# [2 3 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
#  0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
#  0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
# [2 3 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
#  0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
#  0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
# [2 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
#  0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
#  0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]

# 词ID序列转为文本
for document in vocab.reverse(id_documents):
    print(document)
# 这是 一条 测试 <UNK> <UNK> <UNK> ...
# 这是 一条 测试 <UNK> <UNK> <UNK> ...
# 这是 一条 测试 <UNK> <UNK> <UNK> ...
# 这是 <UNK> 测试 <UNK> <UNK> <UNK> ...
