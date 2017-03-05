package com.example.yuichi.japanesechess;

import android.support.v4.app.INotificationSideChannel;

import java.util.ArrayList;
import java.util.InputMismatchException;

/**
 * Created by yuichi on 2017/03/01.
 */

public class BoardManager {
    final int up = -11;
    final int rightup = -10;
    final int leftup = -12;
    final int right = 1;
    final int left = -1;
    final int down = 11;
    final int rightdown = 12;
    final int leftdown = 10;

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

    public int convertOppToOwnViewPlace(int place) {
        return 120 - place;
    }


    public ArrayList<Integer> getListOrNoSizeAsNull(ArrayList<Integer> list) {
        if (list.size() == 0) {
            return null;
        }
        return list;
    }

    public boolean isMovable(int nextPlace) {
        if (isInGameBoard(nextPlace)) {
            if (PiecesID.isOppPiece(mBoardPieces[nextPlace]) || mBoardPieces[nextPlace] == 0) {
                return true;
            }
        }
        return false;
    }

    public boolean isOppMovable(int place) {
        if (isInGameBoard(place)) {
            if (PiecesID.isOwnPiece(mBoardPieces[place]) || mBoardPieces[place] == 0) {
                return true;
            }
        }
        return false;
    }

    public void setContinueMovablePlace(ArrayList<Integer> movable, int place, int direction) {
        for (int i = place + direction; ; i += direction) {
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
            break;
        }
    }

    public ArrayList<Integer> pawnMovablePlace(int place) {
        // 歩の動き
        ArrayList<Integer> movable = new ArrayList<>();
        if (isMovable(place+up)) {
            movable.add(place+up);
        }
        return getListOrNoSizeAsNull(movable);
    }

    public ArrayList<Integer> lanceMovablePlace(int place) {
        // 槍の動き
        ArrayList<Integer> movable = new ArrayList<>();
        setContinueMovablePlace(movable, place, up);
        return getListOrNoSizeAsNull(movable);
    }

    public ArrayList<Integer> knightMovablePlace(int place) {
        // 桂馬の動き
        ArrayList<Integer> movable = new ArrayList<>();
        if (isMovable(place+up+rightup)) {
            movable.add(place+up+rightup);
        }
        if (isMovable(place+up+leftup)) {
            movable.add(place+up+leftup);
        }
        return getListOrNoSizeAsNull(movable);
    }

    public ArrayList<Integer> silverMovablePlace(int place) {
        // 銀の動き
        ArrayList<Integer> movable = new ArrayList<>();
        if (isMovable(place+up)) {
            movable.add(place+up);
        }
        if (isMovable(place+leftup)) {
            movable.add(place+leftup);
        }
        if (isMovable(place+rightup)) {
            movable.add(place+rightup);
        }
        if (isMovable(place+rightdown)) {
            movable.add(place+rightdown);
        }
        if (isMovable(place+leftdown)) {
            movable.add(place+leftdown);
        }
        return getListOrNoSizeAsNull(movable);
    }

    public ArrayList<Integer> goldMovablePlace(int place) {
        // 金の動き
        ArrayList<Integer> movable = new ArrayList<>();
        if (isMovable(place+up)) {
            movable.add(place+up);
        }
        if (isMovable(place+leftup)) {
            movable.add(place+leftup);
        }
        if (isMovable(place+rightup)) {
            movable.add(place+rightup);
        }
        if (isMovable(place+right)) {
            movable.add(place+right);
        }
        if (isMovable(place+left)) {
            movable.add(place+left);
        }
        if (isMovable(place+down)) {
            movable.add(place+down);
        }
        return getListOrNoSizeAsNull(movable);
    }

    public ArrayList<Integer> bishopMovablePlace(int place) {
        // 角の動き
        ArrayList<Integer> movable = new ArrayList<>();
        setContinueMovablePlace(movable, place, rightup);
        setContinueMovablePlace(movable, place, leftup);
        setContinueMovablePlace(movable, place, rightdown);
        setContinueMovablePlace(movable, place, leftdown);
        return getListOrNoSizeAsNull(movable);
    }

    public ArrayList<Integer> bishopPromoteMovablePlace(int place) {
        ArrayList<Integer> movable = bishopMovablePlace(place);
        if (movable == null) {
            movable = new ArrayList<>();
        }
        if (isMovable(place+up)) {
            movable.add(place+up);
        }
        if (isMovable(place+right)) {
            movable.add(place+right);
        }
        if (isMovable(place+down)) {
            movable.add(place+down);
        }
        if (isMovable(place+left)) {
            movable.add(place+left);
        }
        return getListOrNoSizeAsNull(movable);
    }

    public ArrayList<Integer> rookMovablePlace(int place) {
        // 飛車の動き
        ArrayList<Integer> movable = new ArrayList<>();
        setContinueMovablePlace(movable, place, up);
        setContinueMovablePlace(movable, place, down);
        setContinueMovablePlace(movable, place, right);
        setContinueMovablePlace(movable, place, left);
        return getListOrNoSizeAsNull(movable);
    }

    public ArrayList<Integer> rookPromoteMovablePlace(int place) {
        ArrayList<Integer> movable = rookMovablePlace(place);
        if (movable == null) {
            movable = new ArrayList<>();
        }
        if (isMovable(place+rightup)) {
            movable.add(place+rightup);
        }
        if (isMovable(place+rightdown)) {
            movable.add(place+rightdown);
        }
        if (isMovable(place+leftdown)) {
            movable.add(place+leftdown);
        }
        if (isMovable(place+leftup)) {
            movable.add(place+leftup);
        }
        return getListOrNoSizeAsNull(movable);
    }

    public ArrayList<Integer> kingMovablePlace(int place) {
        ArrayList<Integer> movable = new ArrayList<>();
        if (isMovable(place+up)) {
            movable.add(place+up);
        }
        if (isMovable(place+rightup)) {
            movable.add(place+rightup);
        }
        if (isMovable(place+right)) {
            movable.add(place+right);
        }
        if (isMovable(place+rightdown)) {
            movable.add(place+rightdown);
        }
        if (isMovable(place+down)) {
            movable.add(place+down);
        }
        if (isMovable(place+leftdown)) {
            movable.add(place+leftdown);
        }
        if (isMovable(place+left)) {
            movable.add(place+left);
        }
        if (isMovable(place+leftup)) {
            movable.add(place+leftup);
        }
        return getListOrNoSizeAsNull(movable);
    }

    public ArrayList<Integer> oppKingMovablePlace(int place) {
        ArrayList<Integer> movable = new ArrayList<>();
        if (isOppMovable(place+up)) {
            movable.add(place+up);
        }
        if (isOppMovable(place+rightup)) {
            movable.add(place+rightup);
        }
        if (isOppMovable(place+right)) {
            movable.add(place+right);
        }
        if (isOppMovable(place+rightdown)) {
            movable.add(place+rightdown);
        }
        if (isOppMovable(place+down)) {
            movable.add(place+down);
        }
        if (isOppMovable(place+leftdown)) {
            movable.add(place+leftdown);
        }
        if (isOppMovable(place+left)) {
            movable.add(place+left);
        }
        if (isOppMovable(place+leftup)) {
            movable.add(place+leftup);
        }
        return getListOrNoSizeAsNull(movable);
    }

    public ArrayList<Integer> movablePlace(int place) {

        if (!isInGameBoard(place)) {
            return null;
        }
        if (mBoardPieces[place] == PiecesID.OWN_PAWN.getId()) {
            return pawnMovablePlace(place);
        } else if (mBoardPieces[place] == PiecesID.OWN_LANCE.getId()) {
            return lanceMovablePlace(place);
        } else if (mBoardPieces[place] == PiecesID.OWN_KNIGHT.getId()) {
            return knightMovablePlace(place);
        } else if (mBoardPieces[place] == PiecesID.OWN_SILVER.getId()) {
            return silverMovablePlace(place);
        } else if (mBoardPieces[place] == PiecesID.OWN_GOLD.getId()) {
            return goldMovablePlace(place);
        } else if (mBoardPieces[place] == PiecesID.OWN_BISHOP.getId()) {
            return bishopMovablePlace(place);
        } else if (mBoardPieces[place] == PiecesID.OWN_ROOK.getId()) {
            return rookMovablePlace(place);
        } else if (mBoardPieces[place] == PiecesID.OWN_KING.getId()) {
            return kingMovablePlace(place);
        } else if (mBoardPieces[place] < 0) {
            if (mBoardPieces[place] == PiecesID.OWN_PROMOTE_BISHOP.getId()) {
                return bishopPromoteMovablePlace(place);
            } else if (mBoardPieces[place] == PiecesID.OWN_PROMOTE_ROOK.getId()) {
                return rookPromoteMovablePlace(place);
            } else {
                return goldMovablePlace(place);
            }
        }
        return null;
    }

    public ArrayList<Integer> movablePlace(int place, int kind) {
        if (!isInGameBoard(place)) {
            return null;
        }
        if (kind == PiecesID.OWN_PAWN.getId()) {
            return pawnMovablePlace(place);
        } else if (kind == PiecesID.OWN_LANCE.getId()) {
            return lanceMovablePlace(place);
        } else if (kind == PiecesID.OWN_KNIGHT.getId()) {
            return knightMovablePlace(place);
        } else if (kind == PiecesID.OWN_SILVER.getId()) {
            return silverMovablePlace(place);
        } else if (kind == PiecesID.OWN_GOLD.getId()) {
            return goldMovablePlace(place);
        } else if (kind == PiecesID.OWN_BISHOP.getId()) {
            return bishopMovablePlace(place);
        } else if (kind == PiecesID.OWN_ROOK.getId()) {
            return rookMovablePlace(place);
        } else if (kind == PiecesID.OWN_KING.getId()) {
            return kingMovablePlace(place);
        } else if (kind < 0) {
            if (kind == PiecesID.OWN_PROMOTE_BISHOP.getId()) {
                return bishopPromoteMovablePlace(place);
            } else if (kind == PiecesID.OWN_PROMOTE_ROOK.getId()) {
                return rookPromoteMovablePlace(place);
            } else {
                return goldMovablePlace(place);
            }
        }
        return null;
    }

    public boolean isDroppable(int putPlace, int kind) {
        if (kind == PiecesID.OWN_PAWN.getId()) {
            if (isNifu(putPlace, kind)) {
                return false;
            }
            if (isDropPawnCheckmate(putPlace, kind)) {
                return false;
            }
            if (putPlace < 21) {
                return false;
            }
        } else if (kind == PiecesID.OWN_KNIGHT.getId()) {
            if (putPlace < 32) {
                return false;
            }
        } else if (kind == PiecesID.OWN_LANCE.getId()) {
            if (putPlace < 21) {
                return false;
            }
        }
        return true;
    }

    public boolean isNifu(int place, int kind) {
        if (kind != PiecesID.OWN_PAWN.getId()) {
            return false;
        }
        for (int i=place + up; ; i+=up) {
            if (isInGameBoard(i)) {
                if (mBoardPieces[i] == PiecesID.OWN_PAWN.getId()) {
                    return true;
                }
            } else {
                break;
            }
        }
        for (int i=place+down; ; i+=down) {
            if (isInGameBoard(i)) {
                if (mBoardPieces[i] == PiecesID.OWN_PAWN.getId()) {
                    return true;
                }
            } else {
                break;
            }
        }
        return false;
    }

    public boolean isDropPawnCheckmate(int place, int kind) {
        // 打ち歩詰めチェック
        if (kind != PiecesID.OWN_PAWN.getId()) {
            return false;
        }
        if (isInGameBoard(place+up)) {
            if (mBoardPieces[place+up] == PiecesID.OPP_KING.getId()) {
                ArrayList<Integer> oppKingMovable = oppKingMovablePlace(place+up);
                if (oppKingMovable == null || oppKingMovable.size() < 2) {
                    return true;
                }
            }
        }
        return false;
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
