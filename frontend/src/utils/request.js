import axios from "axios";
import { ElMessage } from "element-plus";

const service = axios.create({
  baseURL: "/",
  timeout: 15000
});

service.interceptors.request.use((config) => {
  const token = localStorage.getItem("zb_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

service.interceptors.response.use(
  (response) => {
    const result = response.data;
    if (result.code !== 0) {
      if (result.code === 401) {
        localStorage.removeItem("zb_token");
      }
      ElMessage.error(result.message || "请求失败");
      return Promise.reject(new Error(result.message || "请求失败"));
    }
    return result.data;
  },
  (error) => {
    const message = error?.response?.data?.message || error.message || "请求失败";
    ElMessage.error(message);
    if (error?.response?.status === 401) {
      localStorage.removeItem("zb_token");
    }
    return Promise.reject(error);
  }
);

export default service;
