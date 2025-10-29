const express = require('express');
const router = express.Router();
const authMiddleware = require('../auth'); // Adjust path
const { executeGraphQL } = require('../dgraph'); // Adjust path

/**
 * @route   POST /api/scores
 * @desc    Add a score for a team in a round
 * @access  Private
 */
router.post('/', authMiddleware, async (req, res) => {
  console.log("POST /api/scores Body:", JSON.stringify(req.body, null, 2));
  try {
    const { teamId, roundId, judgeId, scoresByCriteria } = req.body;
    if (!teamId || !roundId || !judgeId || !scoresByCriteria?.length) {
      return res.status(400).json({ msg: "Missing required fields or criteria scores" });
    }
    const total_score = scoresByCriteria.reduce((sum, c) => sum + (parseInt(c.score, 10) || 0), 0);
    const criteriaInput = scoresByCriteria.map(c => ({
      score: parseInt(c.score, 10) || 0,
      criterion: { id: c.criterionId } // Link by ID
    }));
    // Use the single complex input mutation structure
    const mutation = `
      mutation AddScore($input: [AddScoreInput!]!) {
        addScore(input: $input) {
          score {
            id total_score
            team { id name } judge { id name } round { id name }
            scoresByCriteria { id score criterion { id name } }
          }
        }
      }`;
    const variables = {
        input: [{
            team: { id: teamId },
            judge: { id: judgeId },
            round: { id: roundId },
            total_score: total_score,
            scoresByCriteria: criteriaInput
        }]
    };
    console.log("Executing AddScore Mutation with variables:", JSON.stringify(variables, null, 2));
    const data = await executeGraphQL(mutation, variables);
    if (!data || !data.addScore || !data.addScore.score || data.addScore.score.length === 0) {
        throw new Error('Score creation failed in database response.');
    }
    console.log("AddScore Success Response:", JSON.stringify(data, null, 2));
    res.status(201).json(data.addScore.score[0]);
  } catch (error) {
    console.error('Error adding score:', error.message);
    if (error.response?.data?.errors) { console.error("Dgraph Errors:", error.response.data.errors); }
    res.status(500).send('Server Error');
  }
});

/**
 * @route   GET /api/scores/round/:roundId
 * @desc    Get all scores for a specific round by querying the Round first
 * @access  Private
 */
router.get('/round/:roundId', authMiddleware, async (req, res) => {
  console.log(`GET /api/scores/round/${req.params.roundId}`);
  try {
    // --- CORRECTED QUERY ---
    // Query the Round by ID and fetch its associated scores
    const query = `
      query GetScoresViaRound($roundId: ID!) {
        queryRound(filter: { id: [$roundId] }) { # Find the specific round
          id
          name
          scores @cascade { # Get the scores linked to this round
            id
            total_score
            team @cascade { id name college members }
            judge @cascade { id name }
            # round @cascade { id name } # Don't need round info again here
            scoresByCriteria {
              id score
              criterion { id name maxScore }
            }
          }
        }
      }`;
    // --- END CORRECTION ---

    const variables = { roundId: req.params.roundId };
    console.log("Executing GetScoresViaRound Query with variables:", JSON.stringify(variables, null, 2));
    const data = await executeGraphQL(query, variables);

    if (!data.queryRound || data.queryRound.length === 0) {
        return res.status(404).json({ msg: 'Round not found' });
    }
    const scores = data.queryRound[0].scores || [];
    console.log("GetScoresViaRound Success Response Length:", scores.length);
    res.json(scores); // Return just the scores array

  } catch (error) {
    console.error('Error fetching scores for round:', error.message);
    if (error.response?.data?.errors) { console.error("Dgraph Errors:", error.response.data.errors); }
    res.status(500).send('Server Error');
  }
});

// Optional PUT route (ensure filter uses [id])
router.put('/:id', authMiddleware, async (req, res) => {
    // ... logic ...
     const variables = {
        filter: { id: [req.params.id] }, // Correct filter
        // ... set payload ...
    };
    // ... execute ...
});


module.exports = router;