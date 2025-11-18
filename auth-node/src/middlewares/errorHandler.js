export function errorHandler(err, req, res, next) { // eslint-disable-line no-unused-vars
  console.error(err);
  const status = err.status || (err.message?.startsWith('INVALID') ? 401 : 400);
  res.status(status).json({
    error: err.message || 'UNKNOWN_ERROR'
  });
}

