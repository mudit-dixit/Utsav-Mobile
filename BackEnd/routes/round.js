const express = require('express');
const router = express.Router();
const authMiddleware = require('../auth');
const adminMiddleware = require('../middleware/admin');
const { executeGraphQL } = require('../dgraph');

/**
 * @route   POST /api/rounds
 * @desc    Create a new round with criteria
 * @access  Private (Admin)
 */
router.post('/', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const { name, description, date, time, status, criteria } = req.body;

    const criteriaInput = criteria?.map(c => ({
      name: c.name,
      maxScore: c.maxScore
    })) || [];

    const mutation = `
      mutation AddRound($input: [AddRoundInput!]!) {
        addRound(input: $input) {
          round {
            id
            name
            description
            date
            time
            status
            criteria {
              id
              name
              maxScore
            }
          }
        }
      }
    `;

    const variables = {
      input: [{
        name,
        description,
        date,
        time,
        status,
        criteria: criteriaInput
      }]
    };

    const data = await executeGraphQL(mutation, variables);
    res.json(data.addRound.round);
  } catch (error) {
    console.error('Error creating round:', error.message);
    res.status(500).send('Server Error');
  }
});

/**
 * @route   PUT /api/rounds/:id
 * @desc    Edit a round (all fields) and add/remove criteria
 * @access  Private (Admin)
 */
router.put('/:id', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const { name, description, date, time, status, addCriteria = [], removeCriteriaIds = [] } = req.body;

    // Prepare add/remove criteria input
    const setInput = {
      ...(name && { name }),
      ...(description && { description }),
      ...(date && { date }),
      ...(time && { time }),
      ...(status && { status }),
      ...(addCriteria.length > 0 && { criteria: addCriteria.map(c => ({ name: c.name, maxScore: c.maxScore })) })
    };

    const removeInput = removeCriteriaIds.length > 0 ? { criteria: removeCriteriaIds.map(id => ({ id })) } : undefined;

    const mutation = `
      mutation UpdateRound($input: UpdateRoundInput!) {
        updateRound(input: $input) {
          round {
            id
            name
            description
            date
            time
            status
            criteria {
              id
              name
              maxScore
            }
          }
        }
      }
    `;

    const variables = {
      input: {
        filter: { id: { eq: req.params.id } },
        set: Object.keys(setInput).length ? setInput : undefined,
        remove: removeInput
      }
    };

    const data = await executeGraphQL(mutation, variables);
    res.json(data.updateRound.round);
  } catch (error) {
    console.error('Error editing round:', error.message);
    res.status(500).send('Server Error');
  }
});

/**
 * @route   DELETE /api/rounds/:id
 * @desc    Delete a round by ID
 * @access  Private (Admin)
 */
router.delete('/:id', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const mutation = `
      mutation DeleteRound($filter: RoundFilter!) {
        deleteRound(filter: $filter) {
          msg
        }
      }
    `;

    const variables = { filter: { id: { eq: req.params.id } } };
    const data = await executeGraphQL(mutation, variables);
    res.json(data.deleteRound);
  } catch (error) {
    console.error('Error deleting round:', error.message);
    res.status(500).send('Server Error');
  }
});

/**
 * @route   GET /api/rounds
 * @desc    Get all rounds with criteria
 * @access  Private
 */
router.get('/', authMiddleware, async (req, res) => {
  try {
    const query = `
      query GetRounds {
        queryRound {
          id
          name
          description
          date
          time
          status
          criteria {
            id
            name
            maxScore
          }
        }
      }
    `;
    const data = await executeGraphQL(query, {});
    res.json(data.queryRound);
  } catch (error) {
    console.error('Error fetching rounds:', error.message);
    res.status(500).send('Server Error');
  }
});

module.exports = router;
