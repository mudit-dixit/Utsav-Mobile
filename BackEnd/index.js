// 1. Import the necessary libraries
const express = require('express');
const cors = require('cors');
require('dotenv').config();

// 2. Import all the routers
const authRouter = require('./routes/auth');
const teamsRouter = require('./routes/teams');
const judgeRouter = require('./routes/judge');
const userRouter = require('./routes/user');
const roundRouter = require('./routes/round');
const scoreRouter = require('./routes/score');


// 3. Initialize the Express application
const app = express();
const PORT = process.env.PORT || 3000;

// 4. Apply global middleware
app.use(cors());
app.use(express.json());

// 5. Define a simple base route to confirm the server is running
app.get('/', (req, res) => {
  res.send('Utsav API Server is running!');
});


// 6. Use the API Routers
// All requests to /api/auth will be handled by the authRouter
app.use('/api/auth', authRouter);
app.use('/api/teams', teamsRouter);
app.use('/api/judges', judgeRouter);
app.use('/api/users', userRouter);
app.use('/api/rounds', roundRouter);
app.use('/api/scores', scoreRouter);


// 7. Start the server
app.listen(PORT, () => {
  console.log(`Utsav server is running on http://localhost:${PORT}`);
});
