import { createContext, useEffect, useReducer, useRef } from 'react';
import PropTypes from 'prop-types';
import { signUser, singOutUser } from '../helpers/helper-rest-functions';
import { useRouter } from 'next/router';
import {
  updateBalanceInfo,
  updateRole,
  updateToken, updateTokenExpiresAt,
  updateTokenSalt, updateUserId
} from '../helpers/helper-functions';

const HANDLERS = {
  INITIALIZE: 'INITIALIZE',
  SIGN_IN: 'SIGN_IN',
  SIGN_OUT: 'SIGN_OUT',
  UPDATE_AUTH_STATE: 'UPDATE_AUTH_STATE'
};

const initialState = {
  isAuthenticated: false,
  isLoading: true,
  user: null
};

const handlers = {
  [HANDLERS.INITIALIZE]: (state, action) => {
    const user = action.payload;

    return {
      ...state,
      ...(
        // if payload (user) is provided, then is authenticated
        user
          ? ({
            isAuthenticated: true,
            isLoading: false,
            user
          })
          : ({
            isLoading: false
          })
      )
    };
  },
  [HANDLERS.SIGN_IN]: (state, action) => {
    const userData = action.payload;
    updateBalanceInfo({
      ethBalance: userData.ethBalance,
      totalEthProfitEarned: userData.totalEthProfitEarned
    });
    return {
      ...state,
      isAuthenticated: true,
      userData: {
        ...userData
      }
    };
  },
  [HANDLERS.SIGN_OUT]: (state) => {

    return {
      ...state,
      isAuthenticated: false
    };
  },
  [HANDLERS.UPDATE_AUTH_STATE]: (state, action) => {
    const newUserData = action.payload;
    return {
      ...state,
      isAuthenticated: true,
      userData: {
        ...newUserData
      }
    };
  }
};

const reducer = (state, action) => (
  handlers[action.type] ? handlers[action.type](state, action) : state
);

// The role of this context is to propagate authentication state through the App tree.

export const AuthContext = createContext({ undefined });

export const AuthProvider = (props) => {
  const { children } = props;
  const [state, dispatch] = useReducer(reducer, initialState);
  const initialized = useRef(false);
  const router = useRouter();

  const initialize = async () => {
    // Prevent from calling twice in development mode with React.StrictMode enabled
    if (initialized.current) {
      return;
    }
    initialized.current = true;

    let isAuthenticated = false;
    try {
      isAuthenticated =
        window.sessionStorage.getItem('authenticated') === 'true';
    } catch (err) {
      console.error(err);
    }

    if (isAuthenticated) {

      dispatch({
        type: HANDLERS.INITIALIZE
      });
    } else {
      await router.push('/auth/login');
      dispatch({
        type: HANDLERS.INITIALIZE
      });
    }
  };

  useEffect(
    () => {
      initialize();
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    []
  );
  const signInOrRegister = async ( token) => {
    const payload = await signUser(token);
    if (payload) {
      updateTokenSalt(payload['tokenSalt'])
      updateToken(payload.token)
      updateRole(payload.role)
      updateUserId(payload.userId)
      updateTokenExpiresAt(payload['tokenExpiresAt'])
      await beginSignInPhase(payload);
    }
  };

  const beginSignInPhase = async (payload) => {
    dispatch({
      type: HANDLERS.SIGN_IN,
      payload
    });
    await router.push('/');
  };

  const signOut = async () => {
    await singOutUser();
    dispatch({
      type: HANDLERS.SIGN_OUT
    });

  };
  const updateAuthState = async (payload) => {
    await dispatch({
      type: HANDLERS.UPDATE_AUTH_STATE,
      payload
    });
  };

  return (
    <AuthContext.Provider
      value={{
        ...state,
        beginSignInPhase,
        signInOrRegister,
        signOut,
        updateAuthState
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

AuthProvider.propTypes = {
  children: PropTypes.node
};

export const AuthConsumer = AuthContext.Consumer;