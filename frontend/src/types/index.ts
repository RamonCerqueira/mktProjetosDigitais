export type Role = "ADMIN" | "SELLER" | "BUYER";
export type SubscriptionStatus = "ACTIVE" | "PAST_DUE" | "CANCELED";
export type DocumentType = "CPF" | "CNPJ";
export interface User {
  id: number;
  name: string;
  email: string;
  role: Role;
  documentType: DocumentType;
  documentNumber: string;
  postalCode?: string;
  street?: string;
  streetNumber?: string;
  complement?: string;
  neighborhood?: string;
  city?: string;
  state?: string;
  companyName?: string;
  latitude?: number;
  longitude?: number;
}
export interface AuthResponse { accessToken: string; refreshToken: string; user: User; }
export interface Subscription { status: SubscriptionStatus; expiresAt: string | null; price: number; canPublish: boolean; autoRenew: boolean; externalReference: string | null; }
export interface Project { id: number; title: string; description: string; category: string; techStack: string; price: number; monthlyRevenue: number; status: string; sellerId: number; sellerName: string; sellerCity?: string; sellerState?: string; }
export interface Offer { id: number; projectId: number; amount: number; status: string; buyerId: number; sellerId: number; }
export interface Dashboard { user: User; subscription: Subscription; myProjects: Project[]; offers: Offer[]; }
export interface CepLookup { cep: string; street: string; complement: string; neighborhood: string; city: string; state: string; }
export interface CnpjLookup { cnpj: string; companyName: string; tradeName: string; street: string; neighborhood: string; city: string; state: string; postalCode: string; email: string; }
export interface DocumentValidation { document: string; valid: boolean; type: DocumentType; }
export interface ReverseGeocode { latitude: number; longitude: number; city: string; state: string; country: string; }
