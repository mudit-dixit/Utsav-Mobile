const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
require('dotenv').config();
const { executeGraphQL } = require('../dgraph');
const authMiddleware = require('../auth');

/**
 * @route   POST /api/auth/register
 * @desc    Register a new user
 * @access  Public
 */
router.post('/register', async (req, res) => {
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

        // After registering, create a token so the user is immediately logged in
        const payload = { user: { id: data.addUser.user[0].id, role: role } };
        jwt.sign(
            payload,
            process.env.JWT_SECRET,
            { expiresIn: '1h' },
            (err, token) => {
                if (err) throw err;
                res.status(201).json({
                    message: 'User registered successfully!',
                    token: token,
                });
            }
        );
    } catch (error) {
        console.error('Registration Error:', error.message);
        res.status(500).json({ message: 'Error registering user.' });
    }
});

/**
 * @route   POST /api/auth/login
 * @desc    Authenticate user & get token
 * @access  Public
 */
router.post('/login', async (req, res) => {
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
        jwt.sign(
            payload,
            process.env.JWT_SECRET,
            { expiresIn: '1h' },
            (err, token) => {
                if (err) throw err;
                res.json({ message: 'Login successful!', token: token });
            }
        );
    } catch (error) {
        console.error('Login Error:', error.message);
        res.status(500).json({ message: 'Server error during login.' });
    }
});

/**
 * @route   GET /api/auth/me
 * @desc    Get logged in user's data
 * @access  Private
 */
router.get('/me', authMiddleware, async (req, res) => {
    try {
        const query = `
            query GetUserById($id: ID!) {
                queryUser(filter: { id: { eq: $id } }) {
                    id, name, email, role
                }
            }`;
        const variables = { id: req.user.id };
        const data = await executeGraphQL(query, variables);
        res.json(data.queryUser[0]);
    } catch (error) {
        console.error('Get Me Error:', error.message);
        res.status(500).json({ message: 'Server error fetching user data.' });
    }
});


module.exports = router;
