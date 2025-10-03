const express = require('express');
const router = express.Router();
const authMiddleware = require('../auth');
const { executeGraphQL } = require('../dgraph');

/**
 * @route   POST /api/scores
 * @desc    Add a score for a team in a round
 * @access  Private (Judge)
 */
router.post('/', authMiddleware, async (req, res) => {
  try {
    const { teamId, roundId, judgeId, scoresByCriteria } = req.body;

    if (!teamId || !roundId || !judgeId || !scoresByCriteria?.length) {
      return res.status(400).json({ msg: "Missing required fields or criteria scores" });
    }

    // Calculate total score
    const total_score = scoresByCriteria.reduce((sum, c) => sum + c.score, 0);

    // Prepare CriteriaScore objects
    const criteriaInput = scoresByCriteria.map((c, index) => ({
      id: `CS_${Date.now()}_${index}`, // generate unique id for criteria score
      score: c.score,
      criterion: { id: c.criterionId }
    }));

    // Mutation to create the score
    const mutation = `
      mutation AddScore($input: [AddScoreInput!]!) {
        addScore(input: $input) {
          score {
            id
            total_score
            team { id name }
            judge { id name }
            round { id name }
            scoresByCriteria { id score criterion { id name } }
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
    res.json(data.addScore.score);

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
      query GetScores($filter: ScoreFilter!) {
        queryScore(filter: $filter) {
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
    const variables = { filter: { round: { id: { eq: req.params.roundId } } } };
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

    const criteriaInput = scoresByCriteria.map((c) => ({
      id: c.id, // existing criteria score id
      score: c.score
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
        set: { total_score, scoresByCriteria: criteriaInput }
      }
    };

    const data = await executeGraphQL(mutation, variables);
    res.json(data.updateScore.score);
  } catch (error) {
    console.error('Error updating score:', error.message);
    res.status(500).send('Server Error');
  }
});

module.exports = router;
