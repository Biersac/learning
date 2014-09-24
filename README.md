FlightStats Learning
========

Open source machine learning algorithms.

This is alpha-level code at this point. Use at your own risk. Trust nothing. It will probably make your machine explode, and your cat throw up in your shoes.

Your contributions, criticisms, pull requests, patches, etc are very welcome.

So far, we have implemented: 

* ID3 decision tree + training (very simplistic, as the goal was to provide an underlying tree for the Random Forest; no pruning algorithm is implemented, for example)
* Random Forest algorithm, using underlying ID3 trees.

An example of training and testing the random forest implementation is provided in the OptDigitsExample program. In the example, a random forest is trained to recognize hand-written digits from http://archive.ics.uci.edu/ml/datasets (The "Optical Recognition of Handwritten Digits Data Set"). The training and test-set data have the contrast reduced by a factor of 4 to make the random forest trees have fewer branches at each node (up to 4, rather than 16, which would result in trees that are too highly branched for the extent of the training data). The forests that result have a test-set error of approx. 5-6%.
