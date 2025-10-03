// 1. Import the necessary libraries
const express = require('express');
const cors = require('cors');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
require('dotenv').config();
const { executeGraphQL } = require('./dgraph');
const authMiddleware = require('./auth');
const teamsRouter = require('./routes/teams');
const judgeRouter = require('./routes/judge');
const userRouter = require('./routes/user');
const roundRouter = require('./routes/Round');
const scoreRouter = require('./routes/score');


// 2. Initialize the Express application
const app = express();
const PORT = 3000;

// 3. Apply middleware
app.use(cors());
app.use(express.json());

// 4. Define a simple test route
app.get('/', (req, res) => {
  res.send('Hello from the Utsav Server!');
});

// --- AUTHENTICATION ROUTES ---
app.post('/register', async (req, res) => {
  const { name, email, password, role } = req.body;
  if (!name || !email || !password || !role) {
    return res.status(400).json({ message: 'All fields are required.' });
  }
  try {
    const salt = await bcrypt.genSalt(10);
    const hashedPassword = await bcrypt.hash(password, salt);
    const mutation = `
      mutation AddUser($name: String!, $email: String!, $hashedPassword: String!, $role: Role!) {
        addUser(input: [{ name: $name, email: $email, hashedPassword: $hashedPassword, role: $role }]) {
          user { id, name, email }
        }
      }`;
    const variables = { name, email, hashedPassword, role };
    const data = await executeGraphQL(mutation, variables);
    res.status(201).json({ message: 'User registered successfully!', user: data.addUser.user[0] });
  } catch (error) {
    console.error('Registration Error:', error.message);
    res.status(500).json({ message: 'Error registering user.' });
  }
});

app.post('/login', async (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ message: 'Email and password are required.' });
  }
  try {
    const query = `
      query GetUserByEmail($email: String!) {
        queryUser(filter: { email: { eq: $email } }) {
          id, name, email, role, hashedPassword
        }
      }`;
    const variables = { email };
    const data = await executeGraphQL(query, variables);
    const user = data.queryUser && data.queryUser[0];
    if (!user) {
      return res.status(401).json({ message: 'Invalid credentials.' });
    }
    const isMatch = await bcrypt.compare(password, user.hashedPassword);
    if (!isMatch) {
      return res.status(401).json({ message: 'Invalid credentials.' });
    }
    const payload = { user: { id: user.id, role: user.role } };
    jwt.sign(payload, process.env.JWT_SECRET, { expiresIn: '1h' }, (err, token) => {
      if (err) throw err;
      res.json({ message: 'Login successful!', token: token });
    });
  } catch (error) {
    console.error('Login Error:', error.message);
    res.status(500).json({ message: 'Server error during login.' });
  }
});


// --- PROTECTED ROUTE for user data ---
app.get('/api/me', authMiddleware, (req, res) => {
  res.json({
    message: 'User data fetched successfully.',
    user: req.user
  });
});


// --- USE THE API ROUTERS ---
app.use('/api/teams', teamsRouter); // Teams routes
app.use('/api/judges', judgeRouter); // Judges routes
app.use('/api/users', userRouter); // Users routes
app.use('/api/rounds', roundRouter); // Rounds routes
app.use('/api/scores', scoreRouter); // Scores routes



// Start the server
app.listen(PORT, () => {
  console.log(`Utsav server is running on http://localhost:${PORT}`);
});

