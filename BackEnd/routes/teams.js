const express = require('express');
const router = express.Router();
const authMiddleware = require('../auth');
const adminMiddleware = require('../middleware/admin');
const { executeGraphQL } = require('../dgraph');

/**
 * @route   GET /api/teams
 * @desc    Get all teams
 * @access  Private
 */
router.get('/', authMiddleware, async (req, res) => {
  try {
    const query = `
      query GetTeams {
        queryTeam {
          id
          name
          college
          contactName
          contactEmail
          contactPhone
          members
        }
      }
    `;
    const data = await executeGraphQL(query, {});
    res.json(data.queryTeam);
  } catch (error) {
    console.error('Error fetching teams:', error.message);
    res.status(500).send('Server Error');
  }
});

/**
 * @route   GET /api/teams/:id
 * @desc    Get team by ID
 * @access  Private
 */
router.get('/:id', authMiddleware, async (req, res) => {
  try {
    const query = `
      query GetTeamByID($id: ID!) {
        queryTeam(filter: { id: { eq: $id } }) {
          id
          name
          college
          contactName
          contactEmail
          contactPhone
          members
        }
      }
    `;
    const variables = { id: req.params.id };
    const data = await executeGraphQL(query, variables);
    if (!data.queryTeam || data.queryTeam.length === 0) {
      return res.status(404).json({ msg: 'Team not found' });
    }
    res.json(data.queryTeam[0]);
  } catch (error) {
    console.error('Error fetching team:', error.message);
    res.status(500).send('Server Error');
  }
});

/**
 * @route   POST /api/teams
 * @desc    Add a new team
 * @access  Private/Admin
 */
router.post('/', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const { name, college, contactName, contactEmail, contactPhone, members } = req.body;
    const mutation = `
      mutation AddTeam($input: [AddTeamInput!]!) {
        addTeam(input: $input) {
          team {
            id
            name
            college
            members
          }
        }
      }
    `;
    const variables = {
      input: [
        { name, college, contactName, contactEmail, contactPhone, members }
      ]
    };
    const data = await executeGraphQL(mutation, variables);
    res.json(data.addTeam.team[0]);
  } catch (error) {
    console.error('Error adding team:', error.message);
    res.status(500).send('Server Error');
  }
});

/**
 * @route   PUT /api/teams/:id
 * @desc    Edit a team
 * @access  Private/Admin
 */
router.put('/:id', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const { name, college, contactName, contactEmail, contactPhone, members } = req.body;
    const mutation = `
      mutation UpdateTeam($filter: TeamFilter!, $set: UpdateTeamInput!) {
        updateTeam(filter: $filter, set: $set) {
          team {
            id
            name
            college
            members
          }
        }
      }
    `;
    const variables = {
      filter: { id: { eq: req.params.id } },
      set: { name, college, contactName, contactEmail, contactPhone, members }
    };
    const data = await executeGraphQL(mutation, variables);
    if (!data.updateTeam || data.updateTeam.team.length === 0) {
      return res.status(404).json({ msg: 'Team not found' });
    }
    res.json(data.updateTeam.team[0]);
  } catch (error) {
    console.error('Error updating team:', error.message);
    res.status(500).send('Server Error');
  }
});

/**
 * @route   DELETE /api/teams/:id
 * @desc    Delete a team
 * @access  Private/Admin
 */
router.delete('/:id', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const mutation = `
      mutation DeleteTeam($filter: TeamFilter!) {
        deleteTeam(filter: $filter) {
          msg: numUids
        }
      }
    `;
    const variables = { filter: { id: { eq: req.params.id } } };
    const data = await executeGraphQL(mutation, variables);
    if (data.deleteTeam.msg === 0) {
      return res.status(404).json({ msg: 'Team not found or already deleted' });
    }
    res.json({ msg: 'Team deleted successfully' });
  } catch (error) {
    console.error('Error deleting team:', error.message);
    res.status(500).send('Server Error');
  }
});

module.exports = router;
