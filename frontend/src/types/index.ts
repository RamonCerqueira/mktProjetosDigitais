export type Role = "ADMIN" | "SELLER" | "BUYER";
export type SubscriptionStatus = "ACTIVE" | "INACTIVE" | "PAST_DUE" | "CANCELED";
export type DocumentType = "CPF" | "CNPJ";
export interface User { id: number; name: string; email: string; role: Role; documentType: DocumentType; documentNumber: string; }
export interface AuthResponse { accessToken: string; refreshToken: string; user: User; }
export interface Subscription { status: SubscriptionStatus; expiresAt: string | null; price: number; canPublish: boolean; }
export interface Project { id: number; title: string; description: string; category: string; techStack: string; price: number; monthlyRevenue: number; status: string; sellerId: number; sellerName: string; }
export interface Offer { id: number; projectId: number; amount: number; status: string; buyerId: number; sellerId: number; }
export interface Dashboard { user: User; subscription: Subscription; myProjects: Project[]; offers: Offer[]; }
