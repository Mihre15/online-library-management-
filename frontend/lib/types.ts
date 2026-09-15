export type Book = {
  id: number;
  title: string;
  author: string;
  isbn: string;
  category?: string;
  copiesAvailable: number;
  availableCopies?: number;
  totalCopies?: number;
};

export type LoginResponse = {
  token: string;
  studentId: number;
};

export type RegisterResponse = {
  success: boolean;
  message: string;
  studentId?: number;
};

export type BorrowResponse = {
  success: boolean;
  loanId?: number | null;
  denialReason?: string | null;
};

export type ReturnResponse = {
  success: boolean;
  status: string;
  fineCharged: number;
  message: string;
};

export type StudentLoan = {
  loanId: number;
  book: {
    id: number;
    title: string;
    author: string;
    isbn: string;
  };
  status: string;
  dueDate: string;
  fineCharged: number | null;
};

export type Member = {
  id: number;
  fullName: string;
  email: string;
  status: string;
  role: string;
  registeredAt: string;
  outstandingFines: number;
};

export type AdminLoan = {
  loanId: number;
  member: {
    id: number;
    fullName: string;
    email: string;
  };
  book: {
    id: number;
    title: string;
    author: string;
    isbn: string;
  };
  borrowDate: string;
  dueDate: string;
  returnDate: string | null;
  status: string;
  fineAmount: number;
};

export class ApiError extends Error {
  status: number;

  constructor(message: string, status: number) {
    super(message);
    this.status = status;
  }
}
