import axios from "axios";

const readCookie = (name: string) => {
  if (typeof document === "undefined") return undefined;
  return document.cookie
    .split("; ")
    .find((row) => row.startsWith(`${name}=`))
    ?.split("=")[1];
};

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api",
  withCredentials: true,
});

api.interceptors.request.use((config) => {
  if (typeof window !== "undefined") {
    const token = localStorage.getItem("accessToken");
    const csrf = readCookie("XSRF-TOKEN");
    config.headers = config.headers ?? {};
    if (token) config.headers.Authorization = `Bearer ${token}`;
    if (csrf && config.method && config.method.toLowerCase() !== "get") config.headers["X-XSRF-TOKEN"] = decodeURIComponent(csrf);
  }
  return config;
});

export default api;
