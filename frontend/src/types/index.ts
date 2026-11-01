export type Role = 'CUSTOMER' | 'ADMIN';
export interface User { id: string | number; email: string; role: Role; fullName?: string; }
export interface AuthResponse { accessToken: string; tokenType: string; expiresInSeconds: number; user: User; }
export interface ApiErrorBody { timestamp?: string; status?: number; code?: string; message?: string; path?: string; fieldErrors?: Array<{ field?: string; message?: string }>; }
export interface Facility { id: string | number; name: string; sport?: string; sportType?: string; description?: string; address?: string; imageUrl?: string; price?: string; resources?: Resource[]; active?: boolean; }
export interface Resource { id: string | number; name: string; facilityId?: string | number; type?: string; active?: boolean; }
export interface Slot { id: string | number; resourceId?: string | number; resourceName?: string; startTime: string; endTime: string; basePrice: string; available?: boolean; active?: boolean; }
export interface Product { id: string | number; name: string; description?: string; sku?: string; category?: string; price: string; stockQuantity?: number; active?: boolean; imageUrl?: string; }
export interface Profile { id: string | number; fullName: string; dateOfBirth?: string; relationship?: string; ageCategory?: string; }
export interface CartItem { id: string | number; itemType?: 'BOOKING' | 'PRODUCT'; name?: string; description?: string; quantity: number; unitPrice: string; discountAmount?: string; lineTotal: string; facilityName?: string; resourceName?: string; appointmentStart?: string; appointmentEnd?: string; participantName?: string; ageCategory?: string; couponEligible?: boolean; }
export interface Cart { items: CartItem[]; facilitySubtotal?: string; productSubtotal?: string; childDiscount?: string; seniorDiscount?: string; couponDiscount?: string; totalAmount?: string; couponCode?: string; }
export type AnyRecord = Record<string, unknown>;
