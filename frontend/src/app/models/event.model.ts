export interface Event {
  id: number;
  name: string;
  address: string;
  type: string;
  date: string;
  price: number;
  recurrent: boolean;
  locationId: number;
  locationName: string;
  imageUrl?: string;
}

export interface Location {
  id: number;
  name: string;
  description: string;
  address: string;
  totalRating: number;
  type: string;
  imageUrl?: string;
}
