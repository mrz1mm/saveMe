export enum ResourceType {
  FILE = 'FILE',
  FOLDER = 'FOLDER',
}

export enum PermissionType {
  READ = 'READ',
  EDIT = 'EDIT',
}

export interface ShareRequest {
  sharedWithUserId?: number;
  permissionType: PermissionType;
  isPublicLink: boolean;
  expiresAt?: string;
}

export interface ShareResponse {
  id: number;
  resourceId: number;
  resourceType: ResourceType;
  resourceName: string;
  sharedWithUserId?: number;
  sharedWithUsername?: string;
  permissionType: PermissionType;
  isPublicLink: boolean;
  publicLinkToken?: string;
  publicLinkUrl?: string;
  expiresAt?: string;
  createdAt: string;
}
