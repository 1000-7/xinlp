from sklearn.feature_extraction.text import CountVectorizer
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.feature_extraction.text import TfidfVectorizer

corpus = ["I come to China to travel",
          "This is a car polupar in China",
          "I love tea and Apple ",
          "The work is to write some papers in science"]

vectorizer = CountVectorizer()

transformer = TfidfTransformer()
tfidf = transformer.fit_transform(vectorizer.fit_transform(corpus))
print(tfidf)

tfidf2 = TfidfVectorizer()
re = tfidf2.fit_transform(corpus)
print(re)
