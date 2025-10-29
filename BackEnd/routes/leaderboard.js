const express = require('express');
const router = express.Router();
const authMiddleware = require('../auth'); // Adjust path if needed
const { executeGraphQL } = require('../dgraph'); // Adjust path if needed

/**
 * @route   GET /api/leaderboard
 * @desc    Get aggregated scores for all teams across all rounds, sorted
 * @access  Private
 */
router.get('/', authMiddleware, async (req, res) => {
    console.log("GET /api/leaderboard");
    try {
        // 1. Fetch ALL score submissions, including team details
        const query = `
            query GetAllScores {
                queryScore {
                    id
                    total_score
                    team @cascade { # Need team details for aggregation
                        id
                        name
                        # Include members count if needed for display
                        # members
                    }
                    # Include round details if you want to filter/group by round later
                    # round @cascade { id name }
                }
            }`;
        console.log("Executing GetAllScores Query");
        const data = await executeGraphQL(query, {});
        const allScores = data.queryScore || [];
        console.log(`Fetched ${allScores.length} individual score submissions.`);

        // 2. Aggregate scores per team
        const teamTotals = {}; // Use a map: { teamId: { teamName: '...', totalScore: 150, memberCount: 5 }, ... }

        for (const score of allScores) {
            if (score && score.team && score.team.id) {
                const teamId = score.team.id;
                if (!teamTotals[teamId]) {
                    // Initialize team entry if it's the first score for this team
                    teamTotals[teamId] = {
                        teamId: teamId,
                        teamName: score.team.name,
                        // Calculate member count safely
                        memberCount: (score.team.members ? score.team.members.length : 0),
                        totalScore: 0
                    };
                }
                // Add the current score's total to the team's aggregate
                teamTotals[teamId].totalScore += score.total_score;
            }
        }

        // 3. Convert map to an array
        let leaderboard = Object.values(teamTotals);

        // 4. Sort the array by totalScore (descending)
        leaderboard.sort((a, b) => b.totalScore - a.totalScore);

        console.log("Generated Leaderboard:", JSON.stringify(leaderboard, null, 2));
        res.json(leaderboard); // Send the sorted array

    } catch (error) {
        console.error('Error generating leaderboard:', error.message);
        if (error.response?.data?.errors) { console.error("Dgraph Errors:", error.response.data.errors); }
        res.status(500).send('Server Error');
    }
});

module.exports = router;