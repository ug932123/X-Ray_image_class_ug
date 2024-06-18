import os
import numpy as np
import tensorflow as tf
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename
from keras.utils import load_img, img_to_array

app = Flask(__name__)

model = tf.keras.models.load_model("data\Final_Efficientb7_model.h5")
model.load_weights("data\Final_Efficientb7_weights.h5")

categories = ['Not Pnemonia', 'Pnemonia']

UPLOAD_FOLDER = 'uploads'
if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

def allowed_file(filename):
    ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg'}
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

@app.route('/', methods=['POST'])
def hello():
    return jsonify({'Server': 'Uddhav Gupta, 20/03/2024'})

@app.route('/upload', methods=['HEAD'])
def head():
    return 'x' 

@app.route('/upload', methods=['POST'])
def upload_file():

    if 'file' not in request.files:
        return jsonify({'error': 'No file part'})
    file = request.files['file']

    if file and allowed_file(file.filename):
        filename = secure_filename(file.filename)
        file_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
        file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))

        test_image = load_img(file_path, target_size=(224, 224))
        img_array = img_to_array(test_image)
        img_array = np.expand_dims(img_array, axis=0)

        prediction = model.predict(img_array)
        print(prediction)

        confidence = np.array(prediction)
        if confidence[0][0] > confidence[0][1]:
            output_confidence = confidence[0][0]
        else:
            output_confidence = confidence[0][1]
        return jsonify({'message': 'File uploaded successfully', 'filename': filename, 'output': categories[np.argmax(prediction)], 'confidence': str(output_confidence)})
    else:
        return jsonify({'error': 'File type not allowed'})


if __name__ == '__main__':

    file_path = 'uploads\pne_test.jpeg'
    test_image = load_img(file_path, target_size=(224, 224))
    img_array = img_to_array(test_image)
    img_array = np.expand_dims(img_array, axis=0)
    prediction = model.predict(img_array)
    print(prediction)

    app.run(host="0.0.0.0")
    
