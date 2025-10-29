const express = require('express');
const router = express.Router();
const authMiddleware = require('../auth'); // Adjust path if needed
const adminMiddleware = require('../middleware/admin'); // Adjust path if needed
const { executeGraphQL } = require('../dgraph'); // Adjust path if needed

/**
 * @route   POST /api/rounds
 * @desc    Create a new round with criteria
 * @access  Private (Admin)
 */
router.post('/', authMiddleware, adminMiddleware, async (req, res) => {
    console.log("POST /api/rounds Body:", JSON.stringify(req.body, null, 2));
    try {
        const { name, description, date, time, status = "Pending", criteria } = req.body;
        if (!name || !criteria || !Array.isArray(criteria) || criteria.length === 0) {
            return res.status(400).json({ message: 'Name and at least one criterion are required.' });
        }
        const criteriaInput = criteria.map(c => ({
            name: c.name,
            maxScore: parseInt(c.maxScore, 10) || 0
        }));
        // Use the single complex input mutation structure
        const mutation = `
            mutation AddRound($input: [AddRoundInput!]!) {
                addRound(input: $input) {
                    round { id name description date time status criteria { id name maxScore } }
                }
            }`;
        const variables = { input: [{ name, description, date, time, status, criteria: criteriaInput }] };
        console.log("Executing AddRound Mutation with variables:", JSON.stringify(variables, null, 2));
        const data = await executeGraphQL(mutation, variables);
         if (!data || !data.addRound || !data.addRound.round || data.addRound.round.length === 0) {
            throw new Error('Round creation failed in database.');
        }
        console.log("AddRound Success Response:", JSON.stringify(data, null, 2));
        res.status(201).json(data.addRound.round[0]);
    } catch (error) {
        console.error('Error creating round:', error.message);
        if (error.response?.data?.errors) { console.error("Dgraph Errors:", error.response.data.errors); }
        res.status(500).send('Server Error: Could not create round.');
    }
});

/**
 * @route   PUT /api/rounds/:id
 * @desc    Edit round details, add/remove criteria, add/remove teams
 * @access  Private (Admin)
 */
router.put('/:id', authMiddleware, adminMiddleware, async (req, res) => {
     console.log(`PUT /api/rounds/${req.params.id} Body:`, JSON.stringify(req.body, null, 2));
    try {
        const { name, description, date, time, status, addCriteria = [], removeCriteriaIds = [], addTeamIds = [], removeTeamIds = [] } = req.body;
        const setPayload = {};
        if (name !== undefined) setPayload.name = name;
        if (description !== undefined) setPayload.description = description;
        if (date !== undefined) setPayload.date = date;
        if (time !== undefined) setPayload.time = time;
        if (status !== undefined) setPayload.status = status;
        if (addCriteria.length > 0) { setPayload.criteria = addCriteria.map(c => ({ name: c.name, maxScore: parseInt(c.maxScore, 10) || 0 })); }
        if (addTeamIds.length > 0) { setPayload.teams = addTeamIds.map(id => ({ id: id })); }
        const removePayload = {};
        if (removeCriteriaIds.length > 0) { removePayload.criteria = removeCriteriaIds.map(id => ({ id: id })); }
        if (removeTeamIds.length > 0) { removePayload.teams = removeTeamIds.map(id => ({ id: id })); }
        if (Object.keys(setPayload).length === 0 && Object.keys(removePayload).length === 0) { return res.status(400).json({ msg: 'No fields provided for update.' }); }
        const mutation = `
            mutation UpdateRound($filter: RoundFilter!, $set: RoundPatch, $remove: RoundPatch) {
                updateRound(input: { filter: $filter, set: $set, remove: $remove }) {
                    round { id name description date time status criteria { id name maxScore } teams { id name } }
                }
            }`;
        const variables = { filter: { id: [req.params.id] }, set: Object.keys(setPayload).length > 0 ? setPayload : null, remove: Object.keys(removePayload).length > 0 ? removePayload : null };
        console.log("Executing UpdateRound Mutation with variables:", JSON.stringify(variables, null, 2));
        const data = await executeGraphQL(mutation, variables);
        if (!data.updateRound || !data.updateRound.round || data.updateRound.round.length === 0) { return res.status(404).json({ msg: 'Round not found or update failed' }); }
        console.log("UpdateRound Success Response:", JSON.stringify(data, null, 2));
        res.json(data.updateRound.round[0]);
    } catch (error) {
        console.error('Error editing round:', error.message);
        if (error.response?.data?.errors) { console.error("Dgraph Errors:", error.response.data.errors); }
        res.status(500).send('Server Error');
    }
});

/**
 * @route   DELETE /api/rounds/:id
 * @desc    Delete a round by ID
 * @access  Private (Admin)
 */
router.delete('/:id', authMiddleware, adminMiddleware, async (req, res) => {
    console.log(`DELETE /api/rounds/${req.params.id}`);
    try {
        const mutation = ` mutation DeleteRound($filter: RoundFilter!) { deleteRound(filter: $filter) { numUids } }`;
        const variables = { filter: { id: [req.params.id] } }; // Correct filter syntax
        console.log("Executing DeleteRound Mutation with variables:", JSON.stringify(variables, null, 2));
        const data = await executeGraphQL(mutation, variables);
         if (!data || !data.deleteRound || data.deleteRound.numUids === 0) { return res.status(404).json({ msg: 'Round not found or already deleted' }); }
        console.log("DeleteRound Success Response:", JSON.stringify(data, null, 2));
        res.json({ msg: 'Round deleted successfully' });
    } catch (error) {
        console.error('Error deleting round:', error.message);
        if (error.response?.data?.errors) { console.error("Dgraph Errors:", error.response.data.errors); }
        res.status(500).send('Server Error');
    }
});

/**
 * @route   GET /api/rounds
 * @desc    Get all rounds with criteria
 * @access  Private
 */
router.get('/', authMiddleware, async (req, res) => {
    console.log("GET /api/rounds");
    try {
        const query = ` query GetRounds { queryRound { id name description date time status criteria { id name maxScore } } }`;
        console.log("Executing GetRounds Query");
        const data = await executeGraphQL(query, {});
        console.log("GetRounds Success Response Length:", data.queryRound ? data.queryRound.length : 0);
        res.json(data.queryRound || []);
    } catch (error) {
        console.error('Error fetching rounds:', error.message);
        if (error.response?.data?.errors) { console.error("Dgraph Errors:", error.response.data.errors); }
        res.status(500).send('Server Error');
    }
});

/**
 * @route   GET /api/rounds/:id
 * @desc    Get a single round by its ID, including its criteria
 * @access  Private
 */
router.get('/:id', authMiddleware, async (req, res) => {
  console.log(`GET /api/rounds/${req.params.id}`);
  try {
    const query = `
      query GetRoundDetails($id: ID!) {
        queryRound(filter: { id: [$id] }) {
          id name description date time status
          criteria { id name maxScore } # Include criteria
        }
      }
    `;
    const variables = { id: req.params.id };
     console.log("Executing GetRoundDetails Query with variables:", JSON.stringify(variables, null, 2));
    const data = await executeGraphQL(query, variables);
    if (!data.queryRound || data.queryRound.length === 0) { return res.status(404).json({ msg: 'Round not found' }); }
     console.log("GetRoundDetails Success Response:", JSON.stringify(data, null, 2));
    res.json(data.queryRound[0]);
  } catch (error) {
    console.error('Error fetching round details:', error.message);
     if (error.response?.data?.errors) { console.error("Dgraph Errors:", error.response.data.errors); }
    res.status(500).send('Server Error');
  }
});

/**
 * @route   GET /api/rounds/:id/teams
 * @desc    Get all teams registered for a specific round
 * @access  Private
 */
router.get('/:id/teams', authMiddleware, async (req, res) => {
    console.log(`GET /api/rounds/${req.params.id}/teams`);
    try {
        const query = ` query GetRoundWithTeams($id: ID!) { queryRound(filter: { id: [$id] }) { id name teams @cascade { id name college members } } }`;
        const variables = { id: req.params.id };
        console.log("Executing GetRoundWithTeams Query with variables:", JSON.stringify(variables, null, 2));
        const data = await executeGraphQL(query, variables);
        if (!data.queryRound || data.queryRound.length === 0) { return res.status(404).json({ msg: 'Round not found' }); }
        console.log("GetRoundWithTeams Success Response:", JSON.stringify(data, null, 2));
        res.json(data.queryRound[0].teams || []);
    } catch (error) {
        console.error('Error fetching teams for round:', error.message);
        if (error.response?.data?.errors) { console.error("Dgraph Errors:", error.response.data.errors); }
        res.status(500).send('Server Error');
    }
});

module.exports = router;