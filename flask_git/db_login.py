from flask import Flask, request, jsonify, session
from flask_bcrypt import Bcrypt
from flask_sqlalchemy import SQLAlchemy
from flask_ngrok import run_with_ngrok
import uuid
from datetime import timedelta




app = Flask(__name__)
app.secret_key = "mxg1v9ei82c7nhf5dpkjdgficy932123"
bcrypt = Bcrypt(app)

run_with_ngrok(app) 


# Configuration for SQLAlchemy
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///users.db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

db = SQLAlchemy(app)

user_sessions = {}

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

@app.route('/serverstat', methods=['GET'])
def head():
    return jsonify({"Server": "Uddhav Gupta"})

@app.route('/login', methods=['POST'])
def login():
    username = request.json.get('username')
    password = request.json.get('password')
    keep_signed_in = request.json.get("keepsignedin")

    user = User.query.filter_by(username=username).first()

    if user and bcrypt.check_password_hash(user.password, password):
        session_key = str(uuid.uuid4())

        user_sessions[session_key] = {
            "username": username,
            "keep_signed_in": keep_signed_in
        }

        # Set session cookie expiration based on keep_signed_in
        if keep_signed_in:
            session.permanent = True  # Flask sessions are by default permanent
            app.permanent_session_lifetime = timedelta(days=30)  # Keep session for 30 days
        else:
            session.permanent = False

        session['username'] = username
        session['session_key'] = session_key
        print(session_key)

        return jsonify({"message": "Login successful", "Session": "True","session_key":session_key})
    else:
        return jsonify({"message": "Invalid username or password", "Session": "False"})


@app.route('/logout', methods=['GET'])
def logout():

    session_key = session.get('session_key')
    if session_key and session_key in user_sessions:
        del user_sessions[session_key]
    session.clear()
    return jsonify({"message": "Logged out successfully", "Session": "False"})


@app.route('/signup', methods=['POST'])
def signup():
    username = request.json.get('username')
    password = request.json.get('password')

    if not username and not password:
        return jsonify({"message": "Username and password are required","Session": "missing_value"})

    user = User.query.filter_by(username=username).first()
    if user:
        return jsonify({"message": "Username already exists","Session": "existing_user"})
    
    hashed_password = bcrypt.generate_password_hash(password).decode('utf-8')
    new_user = User(username=username, password=hashed_password)
    db.session.add(new_user)
    db.session.commit()

    return jsonify({"message": "User registered successfully","Session": "True"})

if __name__ == '__main__':
    app.run()
