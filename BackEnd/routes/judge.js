const express = require('express');
const router = express.Router();
const authMiddleware = require('../auth');
const adminMiddleware = require('../middleware/admin');
const { executeGraphQL } = require('../dgraph');

/**
 * @route   POST /api/judges
 * @desc    Create a new judge
 * @access  Private (Admin)
 */
router.post('/', async (req, res) => {
// router.post('/', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const { name, email, contactNumber } = req.body;

    const mutation = `
      mutation AddJudge($input: [AddJudgeInput!]!) {
        addJudge(input: $input) {
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
      input: [{ name, email, contactNumber }]
    };

    const data = await executeGraphQL(mutation, variables);
    res.json(data.addJudge.judge);
  } catch (error) {
    console.error('Error creating judge:', error.message);
    res.status(500).send('Server Error');
  }
});

/**
 * @route   GET /api/judges
 * @desc    Get all judges
 * @access  Private
 */
router.get('/', async (req, res) => {
// router.get('/', authMiddleware, async (req, res) => {
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
    res.json(data.queryJudge);
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
      query GetJudge($filter: JudgeFilter!) {
        queryJudge(filter: $filter) {
          id
          name
          email
          contactNumber
        }
      }
    `;
    const variables = { filter: { id: { eq: req.params.id } } };
    const data = await executeGraphQL(query, variables);
    res.json(data.queryJudge[0] || null);
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

    const mutation = `
      mutation UpdateJudge($input: UpdateJudgeInput!) {
        updateJudge(input: $input) {
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
      input: {
        filter: { id: { eq: req.params.id } },
        set: { name, email, contactNumber }
      }
    };

    const data = await executeGraphQL(mutation, variables);
    res.json(data.updateJudge.judge);
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
          msg
        }
      }
    `;
    const variables = { filter: { id: { eq: req.params.id } } };
    const data = await executeGraphQL(mutation, variables);
    res.json(data.deleteJudge);
  } catch (error) {
    console.error('Error deleting judge:', error.message);
    res.status(500).send('Server Error');
  }
});

module.exports = router;
