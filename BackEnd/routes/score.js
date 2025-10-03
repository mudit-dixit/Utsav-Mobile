const express = require('express');
const router = express.Router();
const authMiddleware = require('../auth');
const { executeGraphQL } = require('../dgraph');

/**
 * @route   POST /api/scores
 * @desc    Add a score for a team in a round
 * @access  Private
 */
router.post('/', authMiddleware, async (req, res) => {
  try {
    const { teamId, roundId, judgeId, scoresByCriteria } = req.body;

    if (!teamId || !roundId || !judgeId || !scoresByCriteria?.length) {
      return res.status(400).json({ msg: "Missing required fields or criteria scores" });
    }

    // Calculate total score from the incoming criteria scores
    const total_score = scoresByCriteria.reduce((sum, c) => sum + c.score, 0);

    // Prepare CriteriaScore objects for the mutation.
    // We let Dgraph generate the IDs for these new objects.
    const criteriaInput = scoresByCriteria.map(c => ({
      score: c.score,
      criterion: { id: c.criterionId } // Link to the existing criterion
    }));

    // Mutation to create the score and its nested criteria scores
    const mutation = `
      mutation AddScore($input: [AddScoreInput!]!) {
        addScore(input: $input) {
          score {
            id
            total_score
            team { id name }
            judge { id name }
            round { id name }
            scoresByCriteria {
              id
              score
              criterion { id name }
            }
          }
        }
      }
    `;

    const variables = {
      input: [{
        team: { id: teamId },
        judge: { id: judgeId },
        round: { id: roundId },
        total_score,
        scoresByCriteria: criteriaInput
      }]
    };

    const data = await executeGraphQL(mutation, variables);
    res.status(201).json(data.addScore.score[0]);

  } catch (error) {
    console.error('Error adding score:', error.message);
    res.status(500).send('Server Error');
  }
});

/**
 * @route   GET /api/scores/round/:roundId
 * @desc    Get all scores for a specific round
 * @access  Private
 */
router.get('/round/:roundId', authMiddleware, async (req, res) => {
  try {
    const query = `
      query GetScores($roundId: ID!) {
        queryScore(filter: { round: { id: { eq: $roundId } } }) {
          id
          total_score
          team { id name }
          judge { id name }
          round { id name }
          scoresByCriteria {
            id
            score
            criterion { id name maxScore }
          }
        }
      }
    `;
    const variables = { roundId: req.params.roundId };
    const data = await executeGraphQL(query, variables);
    res.json(data.queryScore);
  } catch (error) {
    console.error('Error fetching scores:', error.message);
    res.status(500).send('Server Error');
  }
});


/**
 * @route   PUT /api/scores/:id
 * @desc    Update an existing score (optional, if judge wants to correct)
 * @access  Private
 */
router.put('/:id', authMiddleware, async (req, res) => {
    try {
      const { scoresByCriteria } = req.body;
  
      if (!scoresByCriteria?.length) {
        return res.status(400).json({ msg: "Criteria scores are required" });
      }
  
      const total_score = scoresByCriteria.reduce((sum, c) => sum + c.score, 0);
  
      // For updating nested objects in Dgraph, you provide a filter for each
      // nested object you want to change, and the new data in a 'set' block.
      const criteriaSetPayload = scoresByCriteria.map((c) => ({
        filter: { id: { eq: c.id } }, // Filter for the existing criteria score ID
        set: { score: c.score }
      }));
  
      const mutation = `
        mutation UpdateScore($input: UpdateScoreInput!) {
          updateScore(input: $input) {
            score {
              id
              total_score
              scoresByCriteria { id score criterion { id name } }
            }
          }
        }
      `;
  
      const variables = {
        input: {
          filter: { id: { eq: req.params.id } },
          set: { 
            total_score,
            scoresByCriteria: criteriaSetPayload
          }
        }
      };
  
      const data = await executeGraphQL(mutation, variables);
      if (!data.updateScore || data.updateScore.score.length === 0) {
        return res.status(404).json({ msg: 'Score not found' });
      }
      res.json(data.updateScore.score[0]);
    } catch (error) {
      console.error('Error updating score:', error.message);
      res.status(500).send('Server Error');
    }
});

module.exports = router;

