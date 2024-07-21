import { createDeposit, EthUsdPrice, UserTransactions } from '../../helpers/helper-rest-functions';
import { useState } from 'react';
import Dialog from '@mui/material/Dialog';
import {
  Box,
  Card, CardActions,
  CardContent,
  CardHeader,
  Divider,
  Stack,
  Unstable_Grid2 as Grid
} from '@mui/material';
import TextField from '@mui/material/TextField';
import {convertToUsdt, ERROR_ICON, toastMessage} from '../../helpers/helper-functions';
import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import { usePrepareSendTransaction, useSendTransaction } from 'wagmi';
import { moonbaseAlpha} from 'viem/chains';
import {
  CRYPTO_TRANSACTIONS_REST_PATH,
  TOAST_GENERAL_ERROR_MESSAGE,
  WALLET_ADDRESS
} from '../../helpers/helper-constants';
import { parseEther } from 'viem';

export const TopUpForm = ({ isTopUpForm, setTopForm }) => {
  const { ethUsdPrice } = EthUsdPrice();
  const [ethAmount, setEthAmount] = useState('0.00');
  const { txMutator } = UserTransactions(CRYPTO_TRANSACTIONS_REST_PATH);
  const { config } = usePrepareSendTransaction({
    chainId: moonbaseAlpha.id,
    to: WALLET_ADDRESS,
    value: parseEther(ethAmount)
  });
  const { isLoading, sendTransactionAsync } = useSendTransaction(config);
  const confirmTopUp = async () => {
    sendTransactionAsync?.().then(async data => {
      const { hash } = data;
      if (hash) {
        await createDeposit( hash);
        await txMutator();
      }
      setTopForm(false);
    }).catch(() => {
      toastMessage(TOAST_GENERAL_ERROR_MESSAGE, ERROR_ICON);
      setTopForm(false);
    });
  };
  const handleClose = () => {
    setTopForm(false);
  };
  return (
    <>
      <Dialog open={isTopUpForm}
              onClose={handleClose}
              fullWidth
      >
        <form
          autoComplete="off"
          noValidate

        >
          <Card>
            <CardHeader
              title="Top you balance with Eth"
            />
            <CardContent sx={{ pt: 0 }}>
              <Box sx={{ m: -1.5 }}>
                <Grid
                  container
                  spacing={3}
                >
                  <Grid
                    xs={12}
                    md={12}
                  >
                    <Stack direction="row"
                           spacing={1}
                           alignItems="center"
                           justifyContent="center"
                    >
                      <TextField
                        sx={{ width: '100%', marginTop: '5px' }}
                        id="amount"
                        label={'Amount to Top Up. Input ETH. Approx USD: ' + convertToUsdt(
                          ethAmount,
                          ethUsdPrice)}
                        type="number"
                        required
                        InputLabelProps={{
                          shrink: true
                        }}
                        value={ethAmount}
                        onChange={(e) => setEthAmount(e.target.value)}
                      />

                    </Stack>
                  </Grid>
                  <Grid
                    xs={12}
                    md={12}
                  >

                  </Grid>
                </Grid>
              </Box>
            </CardContent>
            <Divider/>
            <CardActions sx={{ justifyContent: 'flex-end' }}>

              {isLoading ? <CircularProgress/> : <Button variant="contained"
                                                         disabled={isLoading
                                                           || !sendTransactionAsync}
                                                         onClick={confirmTopUp}>
                Confirm
              </Button>
              }
            </CardActions>
          </Card>
        </form>
      </Dialog>
    </>
  );

};