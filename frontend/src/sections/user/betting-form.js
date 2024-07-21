import Dialog from '@mui/material/Dialog';
import {
  Box,
  Card,
  CardActions,
  CardContent,
  CardHeader,
  Divider, Typography,
  Unstable_Grid2 as Grid
} from '@mui/material';
import CircularProgress from '@mui/material/CircularProgress';
import Button from '@mui/material/Button';
import { useState } from 'react';
import {
  createUserTransaction, EthUsdPrice
} from '../../helpers/helper-rest-functions';
import { convertToUsdt } from '../../helpers/helper-functions';
import { BET_OUTCOME } from '../../helpers/helper-constants';

export const BettingForm = ({
  betId,
  isBettingFormOpen,
  setIsBettingFormOpen,
  title,
  betOutcome,
  ethTicketPrice
}) => {
  const { ethUsdPrice } = EthUsdPrice();
  const [isLoading, setIsLoading] = useState(false);
  const handleClose = () => {
    setIsBettingFormOpen(false);
  };
  const confirm = async () => {
    setIsLoading(true);
    await createUserTransaction(betId, betOutcome ? BET_OUTCOME.WIN : BET_OUTCOME.LOSE);
    setIsLoading(false);
    setIsBettingFormOpen(false);
  };
  return (
    <>
      <Dialog open={isBettingFormOpen}
              onClose={handleClose}
              fullWidth
      >
        <form
          autoComplete="off"
          noValidate

        >
          <Card>
            <CardHeader
              title={title}
            />
            <CardContent sx={{ pt: 0 }}>
              <Box sx={{ m: 0 }}>
                <Grid
                  container
                  spacing={3}
                >
                  <Grid
                    xs={12}
                    md={12}
                  >
                    <Typography variant="text.primary">
                      Predicted Outcome: {betOutcome ? 'Yes' : 'No'}
                    </Typography>
                  </Grid>
                  <Grid
                    xs={12}
                    md={12}
                  >
                    <Typography variant="text.primary">
                      Ticket Price: {ethTicketPrice} ETH ≈ {convertToUsdt(ethTicketPrice,
                      ethUsdPrice)} USD
                    </Typography>
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
                                                         disabled={isLoading}
                                                         onClick={confirm}>
                Buy
              </Button>}
            </CardActions>
          </Card>
        </form>
      </Dialog>
    </>
  );
};