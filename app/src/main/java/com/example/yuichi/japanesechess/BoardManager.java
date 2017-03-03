package com.example.yuichi.japanesechess;

import java.util.ArrayList;

/**
 * Created by yuichi on 2017/03/01.
 */

public class BoardManager {
    public static BoardManager boardManager = null;
    private int[] mBoardPieces = new int[121];      //ボードのデータ一覧どの駒がどこにあるかどうか

    private BoardManager() {
        if (boardManager != null) {
            return;
        }
        boardManager = this;
        initBoardStatus();
    }

    public static BoardManager getInstance() {
        if (boardManager != null) {
            return boardManager;
        }
        boardManager = new BoardManager();
        return boardManager;
    }

    public static BoardManager getNewInstance() {
        deleteBoardManager();
        boardManager = new BoardManager();
        return boardManager;
    }

    public static void deleteBoardManager () {
        boardManager = null;
    }

    private boolean isInBoard(int place) {
        // ボートに入ってるかどうか
        if (place >= 0 && place < 121) {
            return true;
        }
        return false;
    }

    public boolean isInGameBoard(int place) {
        // ゲームボードにあるかどうか
        if (place < 11 * 10 && place > 11 && place % 11 != 0 && place % 11 != 10) {
            return true;
        }
        return false;
    }

    public int getBoardPiece(int place) {
        if (isInBoard(place)) {
            return mBoardPieces[place];
        }
        return PiecesID.OUT_BOARD.getId();
    }

    public void setBoardPiece(int place, int kind) {
        // ゲームボード上のステータスを変更
        if (isInGameBoard(place)) {
            mBoardPieces[place] = kind;
        }
    }

    public void moveBoardPiece(int pastPlace, int postPlace, int kind) {
        // あるピースを移動
        if (isInGameBoard(pastPlace) && isInGameBoard(postPlace)) {
            mBoardPieces[pastPlace] = PiecesID.NOTHING.getId();
            mBoardPieces[postPlace] = kind;
        }
    }



    public ArrayList<Integer> pawnMovablePlace(int place) {
        ArrayList<Integer> movable = new ArrayList<>();
        if (PiecesID.isOppPiece(mBoardPieces[place - 11]) || mBoardPieces[place - 11] == 0) {
            movable.add(place - 11);
            return movable;
        }
        return null;
    }

    public ArrayList<Integer> lanceMovablePlace(int place) {
        ArrayList<Integer> movable = new ArrayList<>();
        for (int i = place - 11; i > 0; i -= 11) {
            if (isInGameBoard(i)) {
                if (mBoardPieces[i] == 0) {
                    movable.add(i);
                    continue;
                } else if (PiecesID.isOppPiece(mBoardPieces[i])) {
                    movable.add(i);
                    break;
                }
                break;
            }
        }
        if (movable.size() == 0) {
            return null;
        }
        return movable;
    }

    public ArrayList<Integer> movablePlace(int place) {

        if (!isInGameBoard(place)) {
            return null;
        }
        if (mBoardPieces[place] == PiecesID.OWN_PAWN.getId()) {
            return pawnMovablePlace(place);
        } else if (mBoardPieces[place] == PiecesID.OWN_LANCE.getId()) {
            return lanceMovablePlace(place);
        }
        return null;
    }

    private void initBoardStatus() {
        for (int i = 0; i < 121; i++) {
            if (i < 11) {
                mBoardPieces[i] = PiecesID.OUT_BOARD.getId();
            } else if (i < 22) {
                if (i == 11 || i == 21) {
                    mBoardPieces[i] = PiecesID.OUT_BOARD.getId();
                } else if (i == 12 || i == 20) {
                    mBoardPieces[i] = PiecesID.OPP_LANCE.getId();
                } else if (i == 13 || i == 19) {
                    mBoardPieces[i] = PiecesID.OPP_KNIGHT.getId();
                } else if (i == 14 || i == 18) {
                    mBoardPieces[i] = PiecesID.OPP_SILVER.getId();
                } else if (i == 15 || i == 17) {
                    mBoardPieces[i] = PiecesID.OPP_GOLD.getId();
                } else if (i == 16) {
                    mBoardPieces[i] = PiecesID.OPP_KING.getId();
                }
            } else if (i < 33) {
                if (i == 24) {
                    mBoardPieces[i] = PiecesID.OPP_ROOK.getId();
                } else if (i == 30) {
                    mBoardPieces[i] = PiecesID.OPP_BISHOP.getId();
                } else if (i == 22 || i == 32) {
                    mBoardPieces[i] = PiecesID.OUT_BOARD.getId();
                } else {
                    mBoardPieces[i] = PiecesID.NOTHING.getId();
                }
            } else if (i < 44) {
                if (i == 33 || i == 43) {
                    mBoardPieces[i] = PiecesID.OUT_BOARD.getId();
                } else {
                    mBoardPieces[i] = PiecesID.OPP_PAWN.getId();
                }
            } else if (i < 77) {
                if (i % 11 == 0 || (i + 1) % 11 == 0) {
                    mBoardPieces[i] = PiecesID.OUT_BOARD.getId();
                } else {
                    mBoardPieces[i] = PiecesID.NOTHING.getId();
                }
            } else if (i < 88) {
                if (i == 77 || i == 87) {
                    mBoardPieces[i] = PiecesID.OUT_BOARD.getId();
                } else {
                    mBoardPieces[i] = PiecesID.OWN_PAWN.getId();
                }
            } else if (i < 99) {
                if (i == 90) {
                    mBoardPieces[i] = PiecesID.OWN_BISHOP.getId();
                } else if (i == 96) {
                    mBoardPieces[i] = PiecesID.OWN_ROOK.getId();
                } else if (i == 88 || i == 98) {
                    mBoardPieces[i] = PiecesID.OUT_BOARD.getId();
                } else {
                    mBoardPieces[i] = PiecesID.NOTHING.getId();
                }
            } else if (i < 110) {
                if (i == 99 || i == 109) {
                    mBoardPieces[i] = PiecesID.OUT_BOARD.getId();
                } else if (i == 100 || i == 108) {
                    mBoardPieces[i] = PiecesID.OWN_LANCE.getId();
                } else if (i == 101 || i == 107) {
                    mBoardPieces[i] = PiecesID.OWN_KNIGHT.getId();
                } else if (i == 102 || i == 106) {
                    mBoardPieces[i] = PiecesID.OWN_SILVER.getId();
                } else if (i == 103 || i == 105) {
                    mBoardPieces[i] = PiecesID.OWN_GOLD.getId();
                } else if (i == 104) {
                    mBoardPieces[i] = PiecesID.OWN_KING.getId();
                }
            } else {
                mBoardPieces[i] = PiecesID.OUT_BOARD.getId();
            }
        }
    }
}
