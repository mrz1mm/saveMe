export interface FileItem {
  id: number;
  originalFileName: string;
  contentType: string;
  size: number;
  ownerId: number;
  ownerUsername: string;
  folderId: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface FolderItem {
  id: number;
  name: string;
  ownerId: number;
  ownerUsername: string;
  parentFolderId: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface FolderRequest {
  name: string;
  parentFolderId: number | null;
}
