import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import {
  createUser,
  findByIdentifier,
  insertLoginHistory,
  setResetCode,
  updateLoginState,
  updatePassword
} from '../repositories/userRepository.js';

const MAX_FAILED_ATTEMPTS = Number(process.env.MAX_FAILED_ATTEMPTS || 5);
const LOCK_DURATION_MINUTES = Number(process.env.LOCK_DURATION_MINUTES || 15);
const RESET_TOKEN_EXPIRY_MINUTES = Number(process.env.RESET_TOKEN_EXPIRY_MINUTES || 15);

export async function register(payload) {
  const existing = await findByIdentifier(payload.email);
  if (existing) {
    const error = new Error('EMAIL_ALREADY_EXISTS');
    error.status = 409;
    throw error;
  }

  const passwordHash = await bcrypt.hash(payload.password, 12);
  await createUser({
    username: payload.username,
    email: payload.email,
    passwordHash,
    name: payload.name,
    lastName: payload.lastName
  });
}

export async function login(identifier, password, context) {
  const user = await findByIdentifier(identifier);
  if (!user) {
    await logAttempt(identifier, 'FAILED', context);
    throw new Error('INVALID_CREDENTIALS');
  }

  if (isLocked(user)) {
    const error = new Error('ACCOUNT_LOCKED');
    error.status = 423;
    throw error;
  }

  const passwordMatches = await bcrypt.compare(password, user.password);
  if (!passwordMatches) {
    await handleFailedAttempt(user, context);
    throw new Error('INVALID_CREDENTIALS');
  }

  await resetLock(user);
  await logAttempt(user.username, 'SUCCESS', context);

  const token = issueToken(user);
  return {
    accessToken: token,
    tokenType: 'Bearer',
    expiresIn: Number(process.env.JWT_EXPIRES_IN || 3600),
    user: {
      id: user.id,
      username: user.username,
      email: user.email,
      avatarUrl: user.avatar_url,
      bio: user.bio
    }
  };
}

export async function forgotPassword(email) {
  const user = await findByIdentifier(email);
  if (!user) {
    const error = new Error('ACCOUNT_NOT_FOUND');
    error.status = 404;
    throw error;
  }

  const code = generateResetCode();
  const expiresAt = new Date(Date.now() + RESET_TOKEN_EXPIRY_MINUTES * 60 * 1000);
  await setResetCode(user.id, code, expiresAt);
  return {
    message: `Verification code: ${code} (valid ${RESET_TOKEN_EXPIRY_MINUTES} minutes)`
  };
}

export async function resetPassword(email, code, newPassword) {
  const user = await findByIdentifier(email);
  if (!user || !user.reset_code || user.reset_code !== code) {
    throw new Error('INVALID_RESET_CODE');
  }
  if (!user.reset_code_expires_at || new Date(user.reset_code_expires_at) < new Date()) {
    throw new Error('RESET_CODE_EXPIRED');
  }

  const hash = await bcrypt.hash(newPassword, 12);
  await updatePassword(user.id, hash);
  return { message: 'Password updated' };
}

async function handleFailedAttempt(user, context) {
  const attempts = Number(user.failed_login_attempts || 0) + 1;
  let lockoutEndTime = user.lockout_end_time;
  if (attempts >= MAX_FAILED_ATTEMPTS) {
    lockoutEndTime = new Date(Date.now() + LOCK_DURATION_MINUTES * 60 * 1000);
  }
  await updateLoginState(user.id, {
    failedLoginAttempts: attempts >= MAX_FAILED_ATTEMPTS ? 0 : attempts,
    lockoutEndTime
  });
  await logAttempt(user.username, 'FAILED', context);
}

async function resetLock(user) {
  await updateLoginState(user.id, {
    failedLoginAttempts: 0,
    lockoutEndTime: null
  });
}

function isLocked(user) {
  if (!user.lockout_end_time) return false;
  return new Date(user.lockout_end_time) > new Date();
}

async function logAttempt(username, status, context) {
  await insertLoginHistory({
    username,
    status,
    ipAddress: context.ip,
    userAgent: context.userAgent
  });
}

function issueToken(user) {
  if (!process.env.JWT_SECRET) {
    throw new Error('JWT_SECRET_NOT_CONFIGURED');
  }
  return jwt.sign(
    {
      sub: user.id,
      username: user.username
    },
    process.env.JWT_SECRET,
    { expiresIn: Number(process.env.JWT_EXPIRES_IN || 3600) }
  );
}

function generateResetCode() {
  return String(Math.floor(100000 + Math.random() * 900000));
}

