import { Router } from 'express';
import { body, validationResult } from 'express-validator';
import {
  forgotPassword,
  login,
  register,
  resetPassword
} from '../services/authService.js';

const router = Router();

const validate = validations => [
  ...validations,
  (req, res, next) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      const err = new Error(errors.array()[0].msg);
      err.status = 400;
      return next(err);
    }
    return next();
  }
];

const handle = fn => async (req, res, next) => {
  try {
    const result = await fn(req, res);
    if (!res.headersSent) {
      res.json(result || { status: 'ok' });
    }
  } catch (err) {
    next(err);
  }
};

router.post(
  '/register',
  ...validate([
    body('username').isLength({ min: 3 }),
    body('email').isEmail(),
    body('password').isLength({ min: 6 })
  ]),
  handle(async req => {
    await register(req.body);
    return { message: 'Registered' };
  })
);

router.post(
  '/login',
  ...validate([
    body('identifier').notEmpty(),
    body('password').notEmpty()
  ]),
  handle(async req => {
    return login(req.body.identifier, req.body.password, {
      ip: req.ip,
      userAgent: req.get('user-agent')
    });
  })
);

router.post(
  '/forgot-password',
  ...validate([body('email').isEmail()]),
  handle(async req => forgotPassword(req.body.email))
);

router.post(
  '/reset-password',
  ...validate([
    body('email').isEmail(),
    body('code').isLength({ min: 4 }),
    body('newPassword').isLength({ min: 6 })
  ]),
  handle(async req => resetPassword(req.body.email, req.body.code, req.body.newPassword))
);

export default router;

