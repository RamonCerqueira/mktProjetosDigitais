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
  active?: boolean;
  blocked?: boolean;
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
export interface Project { id: number; title: string; description: string; category: string; techStack: string; price: number; monthlyRevenue: number; activeUsers?: number | null; status: string; sellerId: number; sellerName: string; sellerCity?: string; sellerState?: string; score?: number; qualification?: string; ranking?: number | null; suggestedPrice?: number | null; suspicious?: boolean; sellerLevel?: string; sellerVerified?: boolean; projectVerified?: boolean; }
export interface Offer { id: number; projectId: number; amount: number; status: string; buyerId: number; buyerName: string; sellerId: number; sellerName: string; proposerId: number; parentOfferId?: number | null; negotiationKey: string; }
export interface Dashboard { user: User; subscription: Subscription; myProjects: Project[]; offers: Offer[]; }
export interface CepLookup { cep: string; street: string; complement: string; neighborhood: string; city: string; state: string; }
export interface CnpjLookup { cnpj: string; companyName: string; tradeName: string; street: string; neighborhood: string; city: string; state: string; postalCode: string; email: string; }
export interface DocumentValidation { document: string; valid: boolean; type: DocumentType; }
export interface ReverseGeocode { latitude: number; longitude: number; city: string; state: string; country: string; }

export interface Transaction { id: number; projectId: number; amount: number; platformFee: number; sellerNetAmount: number; status: "PENDING" | "HELD" | "RELEASED" | "REFUNDED"; checkoutUrl?: string | null; paymentIntentId?: string | null; }

export interface OfferHistory { id: number; offerId: number; actorId: number; actionType: string; amount: number; details: string; createdAt: string; }
export interface Message { id: number; offerId: number; negotiationKey: string; senderId: number; senderName: string; receiverId: number; receiverName: string; content: string; createdAt: string; }
export interface NotificationItem { id: number; type: "NEW_MESSAGE" | "NEW_OFFER" | "OFFER_ACCEPTED" | "OFFER_REJECTED" | "PAYMENT_COMPLETED" | "SUBSCRIPTION_EXPIRING"; title: string; body: string; createdAt: string; readAt?: string | null; }
export interface ProjectAsset { id: number; projectId: number; type: "IMAGE" | "DOCUMENT"; originalFilename: string; contentType: string; sizeBytes: number; createdAt: string; downloadUrl: string; }

export interface TimeSeriesPoint { label: string; value: number; }
export interface AdminOverview { financial: { monthlyRecurringRevenue: number; totalRevenue: number; totalCommission: number; activeSubscriptions: number; churnRate: number; }; conversion: { visitors: number; users: number; subscribers: number; visitorToUserRate: number; userToSubscriberRate: number; retentionRate: number; }; projects: { totalProjects: number; soldProjects: number; suspiciousProjects: number; }; newUsersByDay: TimeSeriesPoint[]; projectsByDay: TimeSeriesPoint[]; topSellers: Array<{ sellerId: number; sellerName: string; soldProjects: number; grossRevenue: number; }>; }
export interface AdminUserSummary { id: number; name: string; email: string; role: Role; roles: Role[]; blocked: boolean; active: boolean; subscriptionStatus: string; createdAt: string; lastLoginAt?: string | null; city?: string; state?: string; }
export interface AuditEntry { id: number; actorEmail?: string; action: string; resourceType: string; resourceId?: string; httpMethod?: string; path?: string; ipAddress?: string; metadata?: string; createdAt: string; }
export interface AdminUserDetail { user: AdminUserSummary; documentType: string; documentNumber: string; postalCode?: string; street?: string; streetNumber?: string; complement?: string; neighborhood?: string; companyName?: string; history: AuditEntry[]; }
export interface AdminProject { id: number; title: string; status: string; verified: boolean; suspicious: boolean; moderationNotes?: string | null; sellerName: string; price: number; monthlyRevenue: number; createdAt: string; }
export interface AdminTransaction { id: number; projectId: number; projectTitle: string; buyerName: string; sellerName: string; amount: number; platformFee: number; sellerNetAmount: number; status: string; createdAt: string; paymentIntentId?: string | null; }
