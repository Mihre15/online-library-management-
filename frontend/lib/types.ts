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

export class ApiError extends Error {
  status: number;

  constructor(message: string, status: number) {
    super(message);
    this.status = status;
  }
}
