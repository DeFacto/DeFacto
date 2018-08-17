from keras.preprocessing.image import ImageDataGenerator
from keras.models import Sequential
from keras.layers import Conv2D, MaxPooling2D
from keras.layers import Activation, Dropout, Flatten, Dense
from keras import backend as K

# dimensions of our images.
img_width, img_height = 500, 500
train_data_dir = '../../data/web_credibility/cnn_train'
test_data_dir = '../../data/web_credibility/cnn_test'
nb_train_samples = 2400
nb_test_samples = 600
epochs = 50
batch_size = 16

datagen = ImageDataGenerator(
        rotation_range=40,
        width_shift_range=0.2,
        height_shift_range=0.2,
        rescale=1./255,
        shear_range=0.2,
        zoom_range=0.2,
        horizontal_flip=True,
        fill_mode='nearest')

news_file = open("../../data/web_credibility/news/url_file.txt","r")
blogs_file = open("../../data/web_credibility/blogs/url_file.txt","r")
blacklisted_file = open("../../data/web_credibility/blacklisted/url_file.txt","r")
