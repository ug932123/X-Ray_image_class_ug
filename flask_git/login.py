from flask import Flask, request, jsonify, session
from flask_bcrypt import Bcrypt

app = Flask(__name__)
app.secret_key = "mxg1v9ei82c7nhf5dpkjdgficy932123"
bcrypt = Bcrypt(app)

users = {
    "user1": {"username": "admin", "password": bcrypt.generate_password_hash("admin123").decode('utf-8')},
    "user2": {"username": "user", "password": bcrypt.generate_password_hash("user").decode('utf-8')}
    }



@app.route('/login', methods=['POST'])
def login():

    username = request.json.get('username')
    password = request.json.get('password')
    username = str(username)
    password = str(password)

    for user_info in users.values():
        if user_info["username"] == username and bcrypt.check_password_hash(user_info["password"], password):
            session['logged_in'] = True
            session['username'] = username
            return jsonify({"message": "Login successful", "Session": "True"}), 200

    return jsonify({"message": "Invalid username or password"}), 401


@app.route('/logout', methods=['GET'])
def logout():
    session.pop('logged_in', None)
    session.pop('username', None)
    return jsonify({"message": "Logout successful","Session":"False"}), 200


@app.route('/signup', methods=['POST'])
def signup():
    username = request.json.get('username')
    password = request.json.get('password')
    hashed_password = bcrypt.generate_password_hash(password, rounds=4).decode('utf-8')

    all_usernames = [user_info["username"] for user_info in users.values()]

    if not username or not password:
        return jsonify({"message": "Username and password are required"}), 400

    if username in all_usernames:
        return jsonify({"message": "Username already exists"}), 400

    users[f"user{len(users) + 1}"] = {"username": username, "password": hashed_password}
    print(users)

    return jsonify({"message": "User registered successfully"}), 201



if __name__ == '__main__':
    app.run(debug=True)
