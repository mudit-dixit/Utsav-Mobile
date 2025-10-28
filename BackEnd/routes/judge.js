const express = require('express');
const router = express.Router();
const authMiddleware = require('../auth'); // Make sure path is correct
const adminMiddleware = require('../middleware/admin'); // Make sure path is correct
const { executeGraphQL } = require('../dgraph'); // Make sure path is correct

/**
 * @route   POST /api/judges
 * @desc    Create a new judge
 * @access  Private (Admin)
 */
// Apply middleware correctly
router.post('/', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const { name, email, contactNumber } = req.body;

    if (!name || !email || !contactNumber) {
        return res.status(400).json({ message: 'Name, email, and contact number are required.' });
    }

    const mutation = `
      mutation AddJudge($name: String!, $email: String, $contactNumber: String) {
        addJudge(input: [{ name: $name, email: $email, contactNumber: $contactNumber }]) {
          judge {
            id
            name
            email
            contactNumber
          }
        }
      }
    `;

    const variables = { name, email, contactNumber };

    const data = await executeGraphQL(mutation, variables);
    // Handle cases where addJudge might fail silently in Dgraph
    if (!data || !data.addJudge || !data.addJudge.judge || data.addJudge.judge.length === 0) {
        throw new Error('Judge creation failed in database.');
    }
    res.status(201).json(data.addJudge.judge[0]);
  } catch (error) {
    console.error('Error creating judge:', error.message);
     // Check for specific Dgraph errors like unique constraint violation if possible
     if (error.message.includes('unique')) { // Example check
         res.status(400).json({ message: 'Judge with this email already exists.' });
     } else {
        res.status(500).send('Server Error');
     }
  }
});

/**
 * @route   GET /api/judges
 * @desc    Get all judges
 * @access  Private
 */
// Apply middleware correctly
router.get('/', authMiddleware, async (req, res) => {
  try {
    const query = `
      query GetJudges {
        queryJudge {
          id
          name
          email
          contactNumber
        }
      }
    `;
    const data = await executeGraphQL(query, {});
    res.json(data.queryJudge || []); // Return empty array if null
  } catch (error) {
    console.error('Error fetching judges:', error.message);
    res.status(500).send('Server Error');
  }
});

/**
 * @route   GET /api/judges/:id
 * @desc    Get judge by ID
 * @access  Private
 */
router.get('/:id', authMiddleware, async (req, res) => {
  try {
    const query = `
      query GetJudge($id: ID!) {
        queryJudge(filter: { id: [$id] }) { # Use array for consistency
          id
          name
          email
          contactNumber
        }
      }
    `;
    const variables = { id: req.params.id };
    const data = await executeGraphQL(query, variables);
    if (!data.queryJudge || data.queryJudge.length === 0) {
        return res.status(404).json({ msg: 'Judge not found' });
    }
    res.json(data.queryJudge[0]);
  } catch (error) {
    console.error('Error fetching judge:', error.message);
    res.status(500).send('Server Error');
  }
});

/**
 * @route   PUT /api/judges/:id
 * @desc    Update a judge
 * @access  Private (Admin)
 */
router.put('/:id', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const { name, email, contactNumber } = req.body;

     // Build the 'set' object dynamically
    const setPayload = {};
    if (name) setPayload.name = name;
    if (email) setPayload.email = email;
    if (contactNumber) setPayload.contactNumber = contactNumber;

    if (Object.keys(setPayload).length === 0) {
        return res.status(400).json({ msg: 'No fields provided for update.' });
    }

    const mutation = `
      mutation UpdateJudge($filter: JudgeFilter!, $set: JudgePatch!) {
        updateJudge(input: { filter: $filter, set: $set }) {
          judge {
            id
            name
            email
            contactNumber
          }
        }
      }
    `;

    const variables = {
      // Use correct array filter for mutations
      filter: { id: [req.params.id] },
      set: setPayload
    };

    const data = await executeGraphQL(mutation, variables);
    if (!data.updateJudge || !data.updateJudge.judge || data.updateJudge.judge.length === 0) {
      return res.status(404).json({ msg: 'Judge not found or update failed' });
    }
    res.json(data.updateJudge.judge[0]);
  } catch (error) {
    console.error('Error updating judge:', error.message);
    res.status(500).send('Server Error');
  }
});

/**
 * @route   DELETE /api/judges/:id
 * @desc    Delete a judge by ID
 * @access  Private (Admin)
 */
router.delete('/:id', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const mutation = `
      mutation DeleteJudge($filter: JudgeFilter!) {
        deleteJudge(filter: $filter) {
          numUids # Use numUids to check if deletion happened
        }
      }
    `;
    // --- THIS IS THE FIX ---
    // Use correct array filter for delete mutations
    const variables = {
        filter: { id: [req.params.id] }
    };
    // --- END OF FIX ---

    const data = await executeGraphQL(mutation, variables);

    if (!data || !data.deleteJudge || data.deleteJudge.numUids === 0) {
        return res.status(404).json({ msg: 'Judge not found or already deleted' });
    }
    res.json({ msg: 'Judge deleted successfully' }); // Consistent success message
  } catch (error) {
    console.error('Error deleting judge:', error.message);
    res.status(500).send('Server Error');
  }
});

module.exports = router;
