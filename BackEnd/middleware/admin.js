/**
 * This middleware checks if the authenticated user has the 'Admin' role.
 * It should be used AFTER the main authMiddleware.
 */
function adminMiddleware(req, res, next) {
  if (req.user && req.user.role === 'Admin') {
    // If user is an Admin, proceed to the next function
    next();
  } else {
    // If not an Admin, deny access
    res.status(403).json({ message: 'Access denied. Admin role required.' });
  }
}

module.exports = adminMiddleware;
