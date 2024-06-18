import os
import secrets
import numpy as np
import random, string
import tensorflow as tf
from flask_bcrypt import Bcrypt
from flask_ngrok import run_with_ngrok
from flask_sqlalchemy import SQLAlchemy
from werkzeug.utils import secure_filename
from keras.utils import load_img, img_to_array
from flask import Flask, request, jsonify, session


app = Flask(__name__)

app.secret_key = ''.join(secrets.choice(string.ascii_uppercase + string.ascii_lowercase + string.digits) for _ in range(64))

bcrypt = Bcrypt(app)

run_with_ngrok(app) 


# Configuration for SQLAlchemy
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///users.db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

db = SQLAlchemy(app)

# Define the User model
class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(80), unique=True, nullable=False)
    password = db.Column(db.String(120), nullable=False)

    def __repr__(self):
        return f'<User {self.username}>'

# Create the database and the User table
with app.app_context():
    db.create_all()


model_efficientnetb7 = tf.keras.models.load_model("data\Final_Efficientb7_model.h5")
model_efficientnetb7.load_weights("data\Final_Efficientb7_weights.h5")

model_vvg16 = tf.keras.models.load_model("data\Final_vvg16_new_model.h5")
model_vvg16.load_weights("data\Final_vgg16_new_weights.h5")

model_resnet50v2 = tf.keras.models.load_model("data\Final_Resnet50v2_new_model.h5")
model_resnet50v2.load_weights("data\Final_Resnetv2_new_weights.h5")

model_efficientnetb3 = tf.keras.models.load_model("data\Final_efficientnetb3_model.h5")
model_efficientnetb3.load_weights("data\Final_efficientnetb3_weights.h5")


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


@app.route('/serverstat', methods=['GET'])
def head():
    return jsonify({"Server": "Uddhav Gupta"})


@app.route('/login', methods=['POST'])
def login():
    username = request.json.get('username')
    password = request.json.get('password')

    user = User.query.filter_by(username=username).first()

    if user and bcrypt.check_password_hash(user.password, password):
        session['logged_in'] = True
        session['username'] = username
        return jsonify({"message": "Login successful", "Session": "True"})
    else:
        return jsonify({"message": "Invalid username or password", "Session": "False"})


@app.route('/logout', methods=['GET'])
def logout():
    if 'logged_in' not in session:
        return jsonify({"message": "No user logged in", "Session": "False"})

    session.pop('logged_in', None)
    session.pop('username', None)
    return jsonify({"message": "Logout successful", "Session": "False"})


@app.route('/signup', methods=['POST'])
def signup():
    username = request.json.get('username')
    password = request.json.get('password')
    hashed_password = bcrypt.generate_password_hash(password).decode('utf-8')

    if not username or not password:
        return jsonify({"message": "Username and password are required","Session": "missing_value"})

    user = User.query.filter_by(username=username).first()
    if user:
        return jsonify({"message": "Username already exists","Session": "existing_user"})

    new_user = User(username=username, password=hashed_password)
    db.session.add(new_user)
    db.session.commit()

    return jsonify({"message": "User registered successfully","Session": "True"})


@app.route('/efficientnetb7', methods=['POST'])
def efficientNetB7():
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

        prediction = model_efficientnetb7.predict(img_array)
        print(prediction)

        confidence = np.array(prediction)
        if confidence[0][0] > confidence[0][1]:
            output_confidence = confidence[0][0]
        else:
            output_confidence = confidence[0][1]
        output_confidence = output_confidence*100
        output_confidence = round(output_confidence,2)
        return jsonify({'message': 'File uploaded successfully', 'filename': filename, 'output': categories[np.argmax(prediction)],
                        'confidence': str(output_confidence)})
    else:
        return jsonify({'error': 'File type not allowed'})


@app.route('/vvg16', methods=['POST'])
def vvg16():
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

        prediction = model_vvg16.predict(img_array)
        print(prediction)

        confidence = np.array(prediction)
        if confidence[0][0] > confidence[0][1]:
            output_confidence = confidence[0][0]
        else:
            output_confidence = confidence[0][1]
        output_confidence = output_confidence*100
        output_confidence = round(output_confidence,2)
        return jsonify({'message': 'File uploaded successfully', 'filename': filename, 'output': categories[np.argmax(prediction)],
                        'confidence': str(output_confidence)})
    else:
        return jsonify({'error': 'File type not allowed'})


@app.route('/resnet50v2', methods=['POST'])
def resnet50v2():
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

        prediction = model_resnet50v2.predict(img_array)
        print(prediction)

        confidence = np.array(prediction)
        if confidence[0][0] > confidence[0][1]:
            output_confidence = confidence[0][0]
        else:
            output_confidence = confidence[0][1]
        output_confidence = output_confidence*100
        output_confidence = round(output_confidence,2)
        return jsonify({'message': 'File uploaded successfully', 'filename': filename, 'output': categories[np.argmax(prediction)],
                        'confidence': str(output_confidence)})
    else:
        return jsonify({'error': 'File type not allowed'})


@app.route('/efficientnetb3', methods=['POST'])
def efficientnetb3():
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

        prediction = model_efficientnetb3.predict(img_array)
        print(prediction)

        confidence = np.array(prediction)
        if confidence[0][0] > confidence[0][1]:
            output_confidence = confidence[0][0]
        else:
            output_confidence = confidence[0][1]
        output_confidence = output_confidence*100
        output_confidence = round(output_confidence,2)
        return jsonify({'message': 'File uploaded successfully', 'filename': filename, 'output': categories[np.argmax(prediction)],
                        'confidence': str(output_confidence)})
    else:
        return jsonify({'error': 'File type not allowed'})


if __name__ == '__main__':

    file_path = 'uploads\pne_test.jpeg'
    test_image = load_img(file_path, target_size=(224, 224))
    img_array = img_to_array(test_image)
    img_array = np.expand_dims(img_array, axis=0)

    prediction_efficientnetb7 = model_efficientnetb7.predict(img_array)
    print(prediction_efficientnetb7)

    prediction_vvg16 = model_vvg16.predict(img_array)
    print(prediction_vvg16)

    prediction_efficientnetb3 = model_efficientnetb3.predict(img_array)
    print(prediction_efficientnetb3)

    prediction_resnet50v2 = model_resnet50v2.predict(img_array)
    print(prediction_resnet50v2)

    app.run() 
    #host='0.0.0.0'
    #ngrok http --domain=national-pleasantly-earwig.ngrok-free.app 5000
