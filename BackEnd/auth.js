const jwt = require('jsonwebtoken');
require('dotenv').config();

/**
 * This middleware function checks for a valid JSON Web Token in the request headers.
 * If the token is valid, it attaches the decoded user payload to the request object.
 * If not, it sends back an unauthorized error.
 */
function authMiddleware(req, res, next) {
  // 1. Get the token from the 'x-auth-token' header
  const token = req.header('x-auth-token');

  // 2. Check if no token is provided
  if (!token) {
    return res.status(401).json({ message: 'No token, authorization denied.' });
  }

  try {
    // 3. Verify the token using our secret key
    const decoded = jwt.verify(token, process.env.JWT_SECRET);

    // 4. If valid, attach the user's info to the request object
    req.user = decoded.user;

    // 5. Pass control to the next middleware or route handler
    next();
  } catch (err) {
    // If token is not valid
    res.status(401).json({ message: 'Token is not valid.' });
  }
}

module.exports = authMiddleware;