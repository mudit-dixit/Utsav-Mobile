const express = require('express');
const router = express.Router();
const authMiddleware = require('../auth');
const adminMiddleware = require('../middleware/admin');
const { executeGraphQL } = require('../dgraph');
const bcrypt = require('bcryptjs'); // Import bcrypt for hashing

/**
 * @route   POST /api/users
 * @desc    Create a new user by an Admin
 * @access  Private (Admin)
 */
router.post('/', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    // Accept a plain-text 'password' from the request body
    const { name, email, contactNumber, role, password } = req.body;

    if (!name || !email || !password || !role) {
        return res.status(400).json({ message: 'Name, email, password, and role are required.' });
    }

    // Hash the password on the server
    const salt = await bcrypt.genSalt(10);
    const hashedPassword = await bcrypt.hash(password, salt);

    const mutation = `
      mutation AddUser($name: String!, $email: String!, $contactNumber: String, $role: Role!, $hashedPassword: String!) {
        addUser(input: [{
            name: $name,
            email: $email,
            contactNumber: $contactNumber,
            role: $role,
            hashedPassword: $hashedPassword
        }]) {
          user {
            id
            name
            email
            role
          }
        }
      }
    `;

    const variables = {
      name,
      email,
      contactNumber,
      role,
      hashedPassword // Pass the new hashedPassword to the database
    };

    const data = await executeGraphQL(mutation, variables);
    res.status(201).json(data.addUser.user[0]);
  } catch (error) {
    console.error('Error creating user:', error.message);
    res.status(500).send('Server Error');
  }
});

/**
 * @route   GET /api/users
 * @desc    Get all users
 * @access  Private
 */
router.get('/', authMiddleware, async (req, res) => {
  try {
    const query = `
      query GetUsers {
        queryUser {
          id
          name
          email
          contactNumber
          role
        }
      }
    `;
    const data = await executeGraphQL(query, {});
    res.json(data.queryUser);
  } catch (error) {
    console.error('Error fetching users:', error.message);
    res.status(500).send('Server Error');
  }
});

/**
 * @route   GET /api/users/:id
 * @desc    Get user by ID
 * @access  Private
 */
router.get('/:id', authMiddleware, async (req, res) => {
  try {
    const query = `
      query GetUser($id: ID!) {
        queryUser(filter: { id: [$id] }) { // Use array for consistency, although eq works here
          id
          name
          email
          contactNumber
          role
        }
      }
    `;
    const variables = { id: req.params.id };
    const data = await executeGraphQL(query, variables);
    if (!data.queryUser || data.queryUser.length === 0) {
      return res.status(404).json({ msg: 'User not found' });
    }
    res.json(data.queryUser[0]);
  } catch (error) {
    console.error('Error fetching user:', error.message);
    res.status(500).send('Server Error');
  }
});

/**
 * @route   PUT /api/users/:id
 * @desc    Update a user
 * @access  Private (Admin)
 */
router.put('/:id', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const { name, email, contactNumber, role, password } = req.body;

    // Build the 'set' object dynamically based on what was provided
    const setPayload = {};
    if (name) setPayload.name = name;
    if (email) setPayload.email = email;
    if (contactNumber) setPayload.contactNumber = contactNumber;
    if (role) setPayload.role = role;


    // If a new password is provided, hash it and add it to the payload
    if (password) {
      const salt = await bcrypt.genSalt(10);
      setPayload.hashedPassword = await bcrypt.hash(password, salt);
    }

    if (Object.keys(setPayload).length === 0) {
        return res.status(400).json({ msg: 'No fields provided for update.' });
    }

    const mutation = `
      mutation UpdateUser($filter: UserFilter!, $set: UserPatch!) {
        updateUser(input: { filter: $filter, set: $set }) {
          user {
            id
            name
            email
            contactNumber
            role
          }
        }
      }
    `;

    const variables = {
      filter: { id: [req.params.id] }, // Use array for consistency
      set: setPayload
    };

    const data = await executeGraphQL(mutation, variables);
     if (!data.updateUser || !data.updateUser.user || data.updateUser.user.length === 0) {
      return res.status(404).json({ msg: 'User not found or update failed' });
    }
    res.json(data.updateUser.user[0]);
  } catch (error) {
    console.error('Error updating user:', error.message);
    res.status(500).send('Server Error');
  }
});


/**
 * @route   DELETE /api/users/:id
 * @desc    Delete a user by ID
 * @access  Private (Admin)
 */
router.delete('/:id', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const mutation = `
      mutation DeleteUser($filter: UserFilter!) {
        deleteUser(filter: $filter) {
          numUids
        }
      }
    `;
    // --- THIS IS THE FIX ---
    // The filter for delete mutations expects an array of IDs.
    const variables = {
      filter: { id: [req.params.id] }
    };
    // --- END OF FIX ---

    const data = await executeGraphQL(mutation, variables);

    // Check if data and deleteUser exist before accessing numUids
    if (!data || !data.deleteUser || data.deleteUser.numUids === 0) {
        return res.status(404).json({ msg: 'User not found or already deleted' });
    }
    res.json({ msg: 'User deleted successfully' }); // Send consistent success message
  } catch (error) {
    console.error('Error deleting user:', error.message);
    res.status(500).send('Server Error');
  }
});

module.exports = router;

