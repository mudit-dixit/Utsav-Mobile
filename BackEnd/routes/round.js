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
    try {
        const { name, description, date, time, status = "Pending", criteria } = req.body;

        if (!name || !criteria || !Array.isArray(criteria) || criteria.length === 0) {
            return res.status(400).json({ message: 'Name and at least one criterion are required.' });
        }

        const criteriaInput = criteria.map(c => ({
            name: c.name,
            maxScore: parseInt(c.maxScore, 10) || 0
        }));

        // --- THIS IS THE FIX ---
        // 1. The mutation now takes a single, complex input variable.
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
            }`;

        // 2. The variables object is structured to perfectly match the $input type.
        const variables = {
            input: [{
                name,
                description,
                date,
                time,
                status,
                criteria: criteriaInput // The nested criteria are part of this single input object
            }]
        };
        // --- END OF FIX ---

        const data = await executeGraphQL(mutation, variables);
         if (!data || !data.addRound || !data.addRound.round || data.addRound.round.length === 0) {
            throw new Error('Round creation failed in database.');
        }
        res.status(201).json(data.addRound.round[0]);
    } catch (error) {
        console.error('Error creating round:', error.message);
        if (error.response && error.response.data && error.response.data.errors) {
            console.error("Dgraph Errors:", error.response.data.errors);
        }
        res.status(500).send('Server Error');
    }
});

/**
 * @route   PUT /api/rounds/:id
 * @desc    Edit round details, add/remove criteria, add/remove teams
 * @access  Private (Admin)
 */
router.put('/:id', authMiddleware, adminMiddleware, async (req, res) => {
    try {
        const {
            name, description, date, time, status,
            addCriteria = [], removeCriteriaIds = [],
            addTeamIds = [], removeTeamIds = [] // New: Add/Remove Teams
        } = req.body;

        // --- Prepare Payload ---
        const setPayload = {};
        if (name) setPayload.name = name;
        if (description) setPayload.description = description;
        if (date) setPayload.date = date;
        if (time) setPayload.time = time;
        if (status) setPayload.status = status;
        // Add new criteria directly in 'set'
        if (addCriteria.length > 0) {
             setPayload.criteria = addCriteria.map(c => ({
                name: c.name,
                maxScore: parseInt(c.maxScore, 10)
            }));
        }
        // Add new teams directly in 'set'
         if (addTeamIds.length > 0) {
            setPayload.teams = addTeamIds.map(id => ({ id: id }));
        }


        const removePayload = {};
        // Remove existing criteria
        if (removeCriteriaIds.length > 0) {
            removePayload.criteria = removeCriteriaIds.map(id => ({ id: id }));
        }
         // Remove existing teams
         if (removeTeamIds.length > 0) {
            removePayload.teams = removeTeamIds.map(id => ({ id: id }));
        }

        if (Object.keys(setPayload).length === 0 && Object.keys(removePayload).length === 0) {
            return res.status(400).json({ msg: 'No fields provided for update.' });
        }

        // --- Define Mutation ---
        const mutation = `
            mutation UpdateRound($filter: RoundFilter!, $set: RoundPatch, $remove: RoundPatch) {
                updateRound(input: { filter: $filter, set: $set, remove: $remove }) {
                    round {
                        id
                        name
                        description
                        date
                        time
                        status
                        criteria { id name maxScore }
                        teams { id name } # Return updated team list
                    }
                }
            }`;

        // --- Prepare Variables ---
        const variables = {
            filter: { id: [req.params.id] }, // Correct filter syntax
            set: Object.keys(setPayload).length > 0 ? setPayload : null,
            remove: Object.keys(removePayload).length > 0 ? removePayload : null
        };

        // --- Execute ---
        const data = await executeGraphQL(mutation, variables);

        if (!data.updateRound || !data.updateRound.round || data.updateRound.round.length === 0) {
            return res.status(404).json({ msg: 'Round not found or update failed' });
        }
        res.json(data.updateRound.round[0]);
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
                    numUids
                }
            }`;

        const variables = { filter: { id: [req.params.id] } }; // Correct filter syntax
        const data = await executeGraphQL(mutation, variables);

         if (!data || !data.deleteRound || data.deleteRound.numUids === 0) {
            return res.status(404).json({ msg: 'Round not found or already deleted' });
        }
        res.json({ msg: 'Round deleted successfully' });
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
                    # Optionally include teams count or basic info here if needed
                }
            }`;
        const data = await executeGraphQL(query, {});
        res.json(data.queryRound || []);
    } catch (error) {
        console.error('Error fetching rounds:', error.message);
        res.status(500).send('Server Error');
    }
});

/**
 * @route   GET /api/rounds/:id/teams
 * @desc    Get all teams registered for a specific round
 * @access  Private
 */
router.get('/:id/teams', authMiddleware, async (req, res) => {
    try {
        const query = `
            query GetRoundWithTeams($id: ID!) {
                queryRound(filter: { id: [$id] }) {
                    id
                    name
                    teams { # Fetch the full team objects
                        id
                        name
                        college
                        members
                    }
                }
            }`;
        const variables = { id: req.params.id };
        const data = await executeGraphQL(query, variables);

        if (!data.queryRound || data.queryRound.length === 0) {
            return res.status(404).json({ msg: 'Round not found' });
        }
        // Return just the array of teams
        res.json(data.queryRound[0].teams || []);
    } catch (error) {
        console.error('Error fetching teams for round:', error.message);
        res.status(500).send('Server Error');
    }
});


module.exports = router;
