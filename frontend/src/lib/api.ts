import axios from "axios";

const readCookie = (name: string) => {
  if (typeof document === "undefined") return undefined;
  return document.cookie
    .split("; ")
    .find((row) => row.startsWith(`${name}=`))
    ?.split("=")[1];
};

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || "http://localhost:3000/api/v1",
  withCredentials: true,
});

api.interceptors.request.use((config) => {
  if (typeof window !== "undefined") {
    const token = localStorage.getItem("accessToken");
    const csrf = readCookie("XSRF-TOKEN");
    config.headers = config.headers ?? {};
    if (token) config.headers.Authorization = `Bearer ${token}`;
    if (csrf && config.method && config.method.toLowerCase() !== "get") config.headers["X-XSRF-TOKEN"] = decodeURIComponent(csrf);
    const userId = localStorage.getItem("userId");
    if (userId) config.headers["x-user-id"] = userId;
  }
  return config;
});

export default api;
