const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
require('dotenv').config();
const { executeGraphQL } = require('../dgraph'); // Assuming dgraph.js is in the parent directory

/**
 * @route   POST /api/auth/register
 * @desc    Register a new user
 * @access  Public
 */
router.post('/register', async (req, res) => {
    console.log('Registration Request Body:', req.body); // Debugging line
    // Get all required fields, including contactNumber
    const { name, email, password, role, contactNumber } = req.body;

    // Updated validation to include contactNumber (optional, adjust if required)
    if (!name || !email || !password || !role) {
        return res.status(400).json({ message: 'Name, email, password, and role are required.' });
    }

    try {
        // --- Check if user already exists (Good practice) ---
        const checkUserQuery = `
            query CheckUser($email: String!) {
                queryUser(filter: { email: { eq: $email } }) {
                    id
                }
            }`;
        const existingUser = await executeGraphQL(checkUserQuery, { email });
        if (existingUser.queryUser && existingUser.queryUser.length > 0) {
            return res.status(400).json({ message: 'User already exists with this email.' });
        }
        // --- End Check ---

        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);

        // Updated mutation to include contactNumber
        const mutation = `
            mutation AddUser($name: String!, $email: String!, $hashedPassword: String!, $role: Role!, $contactNumber: String) {
                addUser(input: [{
                    name: $name,
                    email: $email,
                    hashedPassword: $hashedPassword,
                    role: $role,
                    contactNumber: $contactNumber
                }]) {
                  user {
                    id
                    name
                    email
                    role # Include role in the response payload
                  }
                }
            }`;

        // Updated variables to include contactNumber
        const variables = {
            name,
            email,
            hashedPassword,
            role,
            contactNumber // Pass contactNumber (can be null/undefined if not provided)
        };

        const data = await executeGraphQL(mutation, variables);

        // Ensure user data exists before proceeding
        if (!data || !data.addUser || !data.addUser.user || data.addUser.user.length === 0) {
             throw new Error('User creation failed in database.');
        }

        const newUser = data.addUser.user[0];

        // After registering, create a token so the user is immediately logged in
        const payload = { user: { id: newUser.id, role: newUser.role } }; // Use the role from the response
        jwt.sign(
            payload,
            process.env.JWT_SECRET,
            { expiresIn: '1h' }, // Token expires in 1 hour
            (err, token) => {
                if (err) throw err;
                res.status(201).json({
                    message: 'User registered successfully!',
                    token: token,
                    user: { // Optionally send back basic user info
                        id: newUser.id,
                        name: newUser.name,
                        email: newUser.email,
                        role: newUser.role
                    }
                });
            }
        );
    } catch (error) {
        console.error('Registration Error:', error.message);
        // Provide a more specific error if possible (e.g., duplicate email from Dgraph)
        if (error.message.includes('already exists')) { // Example check
             res.status(400).json({ message: 'Email already in use.' });
        } else {
             res.status(500).json({ message: 'Error registering user.' });
        }
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
                  id
                  name
                  email
                  role
                  hashedPassword
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
                res.json({
                    message: 'Login successful!',
                    token: token,
                    user: { // Send back user info on login
                        id: user.id,
                        name: user.name,
                        email: user.email,
                        role: user.role
                    }
                });
            }
        );
    } catch (error) {
        console.error('Login Error:', error.message);
        res.status(500).json({ message: 'Server error during login.' });
    }
});


module.exports = router;

