// 1. Import the necessary library
const axios = require('axios');

// 2. Define the URL of our Dgraph GraphQL endpoint
const DGRAPH_URL = 'http://localhost:8080/graphql';

/**
 * A helper function to execute a GraphQL query or mutation against our Dgraph database.
 * @param {string} query - The GraphQL query string.
 * @param {object} variables - An object containing the variables for the query.
 * @returns {Promise<object>} - The data returned from the Dgraph server.
 */
async function executeGraphQL(query, variables) {
  try {
    // 3. Make a POST request using axios
    const response = await axios.post(DGRAPH_URL, {
      query: query,
      variables: variables,
    });

    // 4. Check for errors in the response from Dgraph
    if (response.data.errors) {
      console.error('Dgraph Errors:', response.data.errors);
      throw new Error('Error from Dgraph database.');
    }

    // 5. If successful, return the data
    return response.data.data;

  } catch (error) {
    console.error('Error executing GraphQL query:', error.message);
    // Re-throw the error to be handled by the calling function
    throw error;
  }
}

// 6. Export the function so other files can use it
module.exports = { executeGraphQL };