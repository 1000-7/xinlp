# xinlp
Studying "Statistical Learning Methods," I have basically implemented everything from the EM algorithm in Chapter 8 to CRF in Chapter 11. Combining the current deep learning trend, I also implemented Bi-LSTM+CRF for word segmentation.

## 2019.03.21
Implemented a simple LDA model using Gibbs sampling for iterative updates.

## EM and GMM
First, I studied the EM algorithm and implemented the GMM (Gaussian Mixture Model). \
GMM is very similar to k-means. Through personal testing, I found that GMM is very difficult to train for things like distinguishing male and female heights.

### Related Blogs
https://www.unclewang.info/learn/machine-learning/730/ \
https://www.unclewang.info/learn/machine-learning/735/


## Self-implemented HMM Word Segmentation
Implemented the three types of HMM problems (probability, learning, and prediction) based on the "box and ball" problem. \
The main idea is that once the parameters are trained (using parameters from jieba segmentation), the Viterbi algorithm can be used for implementation. \
The HMM parameters used are those from the python jieba segmentation tool. \
I also tried using the Baum-Welch algorithm for parameter training and learning, but found the results to be very poor....
### Related Blogs
https://www.unclewang.info/learn/machine-learning/745/ \
https://www.unclewang.info/learn/machine-learning/749/

## Self-implemented CRF Word Segmentation
The CRF implementation refers to the approaches used in Ansj and Hanlp. \
CRF parameters were obtained by training with CRF++, and these parameters are used for segmentation. \
Defining feature functions for CRF manually is too exhausting; it is essentially feature engineering. I have not implemented the methods for parameter learning. In essence, defining the feature functions is the hard part. The Viterbi algorithm is used for segmentation; learning via CRF is not implemented, and the probabilities are similar to HMM.
### Related Blogs
https://www.unclewang.info/learn/machine-learning/753/

## Self-implemented Bi-LSTM+CRF Word Segmentation
Two versions were implemented: \
The "ugly" version was the first direct implementation. Since I hadn't written much Python properly before, the naming is random and the structure is messy. I searched Baidu and Bing for things I didn't know—basically implementing it as I went, "building roads when encountering mountains and bridges when crossing rivers." However, the code is very concise, has no encapsulation, and actually looks quite fluid. \
The "non-ugly" version was rewritten after finding a very impressive project on GitHub [guillaumegenthial/sequence_tagging](https://github.com/guillaumegenthial/sequence_tagging). I followed the structure of that project, which has very high Python code integrity (I copied many parts directly 😊). The code is clear, and each file has its own specific responsibility, which does justice to Python (an object-oriented dynamic interpreted language).

### Related Blogs
https://www.unclewang.info/learn/machine-learning/756/

## Self-implemented Lucene-supported Tokenizer — XinAnalyzer
While using Lucene, I saw a Chinese word segmenter called `SmartChineseAnalyzer`. The results weren't great, and I discovered it actually used HMM segmentation. I thought, "My goodness," and decided to write one myself... \
2018.12.11  My own HMM segmenter is now supported. \
2018.12.13  Supported CRF segmentation (via TCP communication) and BiLSTM+CRF segmentation (via HTTP communication).
### Related Blogs
https://www.unclewang.info/learn/java/760/

## Various Data Used
Link: https://pan.baidu.com/s/1toe-0h4k9Ck_yGs-RwMqAA  Password: sn7o
