export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface LoginResponse {
  userId: number;
  username: string;
  email: string;
  nombre: string;
  roles: string[];
  token: string;
  expiresAt: string; // Using string for OffsetDateTime for simplicity, can be converted to Date object if needed
}
